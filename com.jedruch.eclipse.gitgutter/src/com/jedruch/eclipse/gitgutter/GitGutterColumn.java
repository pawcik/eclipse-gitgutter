package com.jedruch.eclipse.gitgutter;

import java.util.Iterator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn;

public class GitGutterColumn extends AbstractContributedRulerColumn {

    private GitGutterRulerColumn delegate = new GitGutterRulerColumn();

    class GitDiffAnnotationModel implements IAnnotationModel {

		@Override
		public void addAnnotationModelListener(IAnnotationModelListener listener) {
			
		}

		@Override
		public void removeAnnotationModelListener(
				IAnnotationModelListener listener) {
			
		}

		@Override
		public void connect(IDocument document) {
			
		}

		@Override
		public void disconnect(IDocument document) {
			
		}

		@Override
		public void addAnnotation(Annotation annotation, Position position) {
			
		}

		@Override
		public void removeAnnotation(Annotation annotation) {
			
		}

		@Override
		public Iterator getAnnotationIterator() {
			return null;
		}

		@Override
		public Position getPosition(Annotation annotation) {
			return null;
		}
    	
    }
    @Override
    public void setModel(IAnnotationModel model) {
    	new GitDiffAnnotationModel();
        delegate.setModel(model);
    }

    @Override
    public void redraw() {
        delegate.redraw();
    }

    @Override
    public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
        return delegate.createControl(parentRuler, parentControl);
    }

    @Override
    public Control getControl() {
        return delegate.getControl();
    }

    @Override
    public int getWidth() {
        return delegate.getWidth();
    }

    @Override
    public void setFont(Font font) {
        delegate.setFont(font);
    }

}
