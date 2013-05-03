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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * IAnnotationModel implementation for efficient coverage highlighting.
 */
public final class DiffAnnotationModel implements IAnnotationModel {

	/** Key used to piggyback our model to the editor's model. */
	private static final Object KEY = new Object();

	/** List of current CoverageAnnotation objects */
	private List<DiffAnnotation> annotations = new ArrayList<DiffAnnotation>(32);

	/** List of registered IAnnotationModelListener */
	private ListenerList annotationModelListeners = new ListenerList();

	private final ITextEditor editor;
	private final IDocument document;
	private int openConnections = 0;
	private boolean annotated = false;

	private IDocumentListener documentListener = new IDocumentListener() {
		public void documentChanged(DocumentEvent event) {
			updateAnnotations(false);
		}

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
		IAnnotationModel model = provider.getAnnotationModel(editor
				.getEditorInput());
		if (!(model instanceof IAnnotationModelExtension))
			return;
		IAnnotationModelExtension modelex = (IAnnotationModelExtension) model;

		IDocument document = provider.getDocument(editor.getEditorInput());

		DiffAnnotationModel coveragemodel = (DiffAnnotationModel) modelex
				.getAnnotationModel(KEY);
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
		IAnnotationModel model = provider.getAnnotationModel(editor
				.getEditorInput());
		if (!(model instanceof IAnnotationModelExtension))
			return;
		IAnnotationModelExtension modelex = (IAnnotationModelExtension) model;
		modelex.removeAnnotationModel(KEY);
	}

	private void updateAnnotations(boolean force) {
		annotations.clear();
		annotations.add(new DiffAnnotation());
		// final ISourceNode coverage = findSourceCoverageForEditor();
		// if (coverage != null) {
		// if (!annotated || force) {
		// createAnnotations(coverage);
		// annotated = true;
		// }
		// } else {
		// if (annotated) {
		// clear();
		// annotated = false;
		// }
		// }
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

	public void addAnnotationModelListener(IAnnotationModelListener listener) {
		annotationModelListeners.add(listener);
		fireModelChanged(new AnnotationModelEvent(this, true));
	}

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

	public void disconnect(IDocument document) {
		if (this.document != document)
			throw new RuntimeException(
					"Can't disconnect from different document."); //$NON-NLS-1$
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
	public void addAnnotation(Annotation annotation, Position position) {
		throw new UnsupportedOperationException();
	}

	/**
	 * External modification is not supported.
	 */
	public void removeAnnotation(Annotation annotation) {
		throw new UnsupportedOperationException();
	}

	public Iterator<?> getAnnotationIterator() {
		return annotations.iterator();
	}

	public Position getPosition(Annotation annotation) {
		if (annotation instanceof DiffAnnotation) {
			return ((DiffAnnotation) annotation).getPosition();
		} else {
			return null;
		}
	}

}
