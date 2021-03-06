/*******************************************************************************
 * Copyright (c) 2006, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 ******************************************************************************/
package com.jedruch.eclipse.gitgutter.ui.annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.egit.core.GitProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.jedruch.eclipse.gitgutter.ui.annotation.DiffAnnotation.DiffType;

/**
 * IAnnotationModel implementation for efficient coverage highlighting.
 */
public final class DiffAnnotationModel implements IAnnotationModel {

    /** Key used to piggyback our model to the editor's model. */
    public static final Object KEY = new Object();

    /** List of current CoverageAnnotation objects */
    private final List<DiffAnnotation> annotations = new ArrayList<DiffAnnotation>(32);

    /** List of registered IAnnotationModelListener */
    private final ListenerList annotationModelListeners = new ListenerList();

    private final ITextEditor editor;
    private final IDocument document;
    private int openConnections = 0;
    private final boolean annotated = false;

    private final IDocumentListener documentListener = new IDocumentListener() {
        @Override
        public void documentChanged(DocumentEvent event) {
            updateAnnotations(false);
        }

        @Override
        public void documentAboutToBeChanged(DocumentEvent event) {
        }
    };

    private DiffAnnotationModel(ITextEditor editor, IDocument document) {
        this.editor = editor;
        this.document = document;
        updateAnnotations(true);
    }

    /**
     * Attaches a coverage annotation model for the given editor if the editor
     * can be annotated. Does nothing if the model is already attached.
     * 
     * @param editor
     *            Editor to attach a annotation model to
     */
    public static void attach(ITextEditor editor) {
        IDocumentProvider provider = editor.getDocumentProvider();
        if (provider == null)
            return;
        IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
        if (!(model instanceof IAnnotationModelExtension))
            return;
        IAnnotationModelExtension modelex = (IAnnotationModelExtension) model;

        IDocument document = provider.getDocument(editor.getEditorInput());

        DiffAnnotationModel coveragemodel = (DiffAnnotationModel) modelex.getAnnotationModel(KEY);
        if (coveragemodel == null) {
            coveragemodel = new DiffAnnotationModel(editor, document);
            modelex.addAnnotationModel(KEY, coveragemodel);
        }
    }

    /**
     * Detaches the coverage annotation model from the given editor. If the
     * editor does not have a model attached, this method does nothing.
     * 
     * @param editor
     *            Editor to detach the annotation model from
     */
    public static void detach(ITextEditor editor) {
        IDocumentProvider provider = editor.getDocumentProvider();
        // there may be text editors without document providers (SF #1725100)
        if (provider == null)
            return;
        IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
        if (!(model instanceof IAnnotationModelExtension))
            return;
        IAnnotationModelExtension modelex = (IAnnotationModelExtension) model;
        modelex.removeAnnotationModel(KEY);
    }

    private void updateAnnotations(boolean force) {
        annotations.clear();
        annotations.addAll(createAnnotations(editor));
        fireModelChanged(new AnnotationModelEvent(this, true));
    }

    private Collection<? extends DiffAnnotation> createAnnotations(ITextEditor editor2) {
        IEditorInput input = editor2.getEditorInput();
        if (input instanceof FileEditorInput) {
            IProject project = ((FileEditorInput) input).getFile().getProject();
            RepositoryProvider.getProvider(project, GitProvider.ID);
            GitProvider gitProvider = (GitProvider) RepositoryProvider.getProvider(project);
            if (gitProvider != null) {
                try {
                    Repository repository = gitProvider.getData().getRepositoryMapping(project)
                            .getRepository();
                    String path = ((FileEditorInput) input).getFile().getFullPath().makeRelative()
                            .toString();
                    RawText old = new RawText(fetchBlob(path, repository));
                    RawText neww = new RawText(document.get().getBytes());
                    EditList edits = new HistogramDiff().diff(RawTextComparator.WS_IGNORE_ALL, old,
                            neww);
                    return createAnnotations(edits);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }

    private byte[] fetchBlob(String path, Repository repo) throws RevisionSyntaxException,
            AmbiguousObjectException, IncorrectObjectTypeException, IOException {
        // Resolve the revision specification
        final ObjectId id = repo.resolve(Constants.HEAD);

        // Get the commit object for that revision
        RevWalk walk = new RevWalk(repo);
        RevCommit commit = walk.parseCommit(id);

        // Get the commit's file tree
        RevTree tree = commit.getTree();
        // .. and narrow it down to the single file's path
        TreeWalk treewalk = TreeWalk.forPath(repo, path, tree);

        if (treewalk != null) { // if the file exists in that commit
            // use the blob id to read the file's data
            return repo.open(treewalk.getObjectId(0)).getBytes();
        } else {
            return new byte[0];
        }
    }

    /**
     * An edit where beginA == endA && beginB < endB is an insert edit, that is
     * sequence B inserted the elements in region [beginB, endB) at beginA.
     * 
     * An edit where beginA < endA && beginB == endB is a delete edit, that is
     * sequence B has removed the elements between [beginA, endA).
     * 
     * An edit where beginA < endA && beginB < endB is a replace edit, that is
     * sequence B has replaced the range of elements between [beginA, endA)
     * 
     * @param edits
     * @return
     */
    private Collection<? extends DiffAnnotation> createAnnotations(EditList edits) {
        List<DiffAnnotation> list = new ArrayList<DiffAnnotation>();
        for (Edit e : edits) {
            switch (e.getType()) {
            case INSERT:
                try {
                    IRegion lineB = document.getLineInformation(e.getBeginB());
                    IRegion lineE = document.getLineInformation(e.getEndB() - 1);
                    int end = 0;// lineE.getLength() + lineE.getOffset() +
                                // lineB.getLength();
                    AbstractTextEditor aa = null;
                    list.add(new DiffAnnotation(DiffType.ADDED, lineB.getOffset(), end, e.getEndB()
                            - e.getBeginB()));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                break;
            case DELETE:
                try {
                    IRegion lineB = document.getLineInformation(e.getBeginB());
                    list.add(new DiffAnnotation(DiffType.DELETED, lineB.getOffset(), 0, 1));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                break;
            case REPLACE:
                try {
                    IRegion lineB = document.getLineInformation(e.getBeginB());
                    IRegion lineE = document.getLineInformation(e.getEndB() - 1);
                    int end = lineE.getOffset() + lineE.getLength();
                    list.add(new DiffAnnotation(DiffType.MODIFIED, lineB.getOffset(), end, e
                            .getEndB() - e.getBeginB()));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                break;
            case EMPTY:
            }
        }
        return list;
    }

    protected static byte[] getBytes(final Repository repository, final ObjectId id) {
        try {
            return repository.open(id, Constants.OBJ_BLOB).getCachedBytes(Integer.MAX_VALUE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AbstractTreeIterator getTreeIterator(String name, Repository repository)
            throws IOException {
        final ObjectId id = repository.resolve(name);
        if (id == null)
            throw new IllegalArgumentException(name);
        final CanonicalTreeParser p = new CanonicalTreeParser();
        final ObjectReader or = repository.newObjectReader();
        try {
            p.reset(or, new RevWalk(repository).parseTree(id));
            return p;
        } finally {
            or.release();
        }
    }

    private void clear() {
        AnnotationModelEvent event = new AnnotationModelEvent(this);
        clear(event);
        fireModelChanged(event);
    }

    private void clear(AnnotationModelEvent event) {
        for (final DiffAnnotation ca : annotations) {
            event.annotationRemoved(ca, ca.getPosition());
        }
        annotations.clear();
    }

    @Override
    public void addAnnotationModelListener(IAnnotationModelListener listener) {
        annotationModelListeners.add(listener);
        fireModelChanged(new AnnotationModelEvent(this, true));
    }

    @Override
    public void removeAnnotationModelListener(IAnnotationModelListener listener) {
        annotationModelListeners.remove(listener);
    }

    private void fireModelChanged(AnnotationModelEvent event) {
        event.markSealed();
        if (!event.isEmpty()) {
            for (final Object l : annotationModelListeners.getListeners()) {
                if (l instanceof IAnnotationModelListenerExtension) {
                    ((IAnnotationModelListenerExtension) l).modelChanged(event);
                } else {
                    ((IAnnotationModelListener) l).modelChanged(this);
                }
            }
        }
    }

    @Override
    public void connect(IDocument document) {
        if (this.document != document)
            throw new RuntimeException("Can't connect to different document."); //$NON-NLS-1$
        for (final DiffAnnotation ca : annotations) {
            try {
                document.addPosition(ca.getPosition());
            } catch (BadLocationException ex) {
                // EclEmmaUIPlugin.log(ex);
            }
        }
        if (openConnections++ == 0) {
            document.addDocumentListener(documentListener);
        }
    }

    @Override
    public void disconnect(IDocument document) {
        if (this.document != document)
            throw new RuntimeException("Can't disconnect from different document."); //$NON-NLS-1$
        for (final DiffAnnotation ca : annotations) {
            document.removePosition(ca.getPosition());
        }
        if (--openConnections == 0) {
            document.removeDocumentListener(documentListener);
        }
    }

    /**
     * External modification is not supported.
     */
    @Override
    public void addAnnotation(Annotation annotation, Position position) {
        throw new UnsupportedOperationException();
    }

    /**
     * External modification is not supported.
     */
    @Override
    public void removeAnnotation(Annotation annotation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<?> getAnnotationIterator() {
        return annotations.iterator();
    }

    @Override
    public Position getPosition(Annotation annotation) {
        if (annotation instanceof DiffAnnotation) {
            return ((DiffAnnotation) annotation).getPosition();
        } else {
            return null;
        }
    }

}
