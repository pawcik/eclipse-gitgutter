package com.jedruch.eclipse.gitgutter.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

import com.jedruch.eclipse.gitgutter.GitGutterPlugin;
import com.jedruch.eclipse.gitgutter.ui.annotation.DiffAnnotation;

public class AnnotationImageProvider implements IAnnotationImageProvider {

    public AnnotationImageProvider() {
    }

    @Override
    public Image getManagedImage(Annotation annotation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getImageDescriptorId(Annotation annotation) {
        if (annotation instanceof DiffAnnotation) {
            DiffAnnotation a = (DiffAnnotation) annotation;
            switch (a.getDiffType()) {
            case ADDED:
                return GitGutterPlugin.IMG_ADDED;
            case DELETED:
                return GitGutterPlugin.IMG_DELETED;
            case MODIFIED:
                return GitGutterPlugin.IMG_MODIFIED;
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getImageDescriptor(String imageDescritporId) {
        return GitGutterPlugin.getImageDescriptor(imageDescritporId);
    }

}
