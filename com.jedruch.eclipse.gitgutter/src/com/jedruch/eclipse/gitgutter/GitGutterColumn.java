package com.jedruch.eclipse.gitgutter;

import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn;

import com.jedruch.eclipse.gitgutter.ui.annotation.DiffAnnotationModel;

public class GitGutterColumn extends AbstractContributedRulerColumn {

    private GitGutterRulerColumn delegate = new GitGutterRulerColumn();
	private ISourceViewer viewer;

    @Override
    public void setModel(IAnnotationModel model) {
        delegate.setModel(model);
    }

    @Override
    public void redraw() {
        delegate.redraw();
    }

    @Override
    public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
    	ITextViewer viewer= parentRuler.getTextViewer();
		Assert.isLegal(viewer instanceof ISourceViewer);
		this.viewer= (ISourceViewer) viewer;
		setModel(getDiffAnnotationModel());
    
        Control control  = delegate.createControl(parentRuler, parentControl);
        return control;
    }

    private IAnnotationModel getDiffAnnotationModel() {
    	IAnnotationModel m =  viewer.getAnnotationModel();
    	if (!(m instanceof IAnnotationModelExtension))
			return null;
    	
    	IAnnotationModelExtension model = (IAnnotationModelExtension) m;
    	return model.getAnnotationModel(DiffAnnotationModel.KEY);
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
