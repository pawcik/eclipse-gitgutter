package com.jedruch.eclipse.gitgutter.ui.annotation;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

public class DiffAnnotation extends Annotation implements IAnnotationPresentation {

    private static final String TYPE = "com.jedruch.eclipse.gitgutter.diff";

    public enum DiffType {
        ADDED("+", SWT.COLOR_DARK_GREEN),
        DELETED("▬", SWT.COLOR_RED),
        MODIFIED("▄", SWT.COLOR_DARK_MAGENTA);

        private final String label;
        private int color;

        DiffType(String label, int c) {
            this.label = label;
            this.color = c;
        }

        public String getLabel() {
            return label;
        }

        public Color getColor() {
            return Display.getDefault().getSystemColor(color);
        }

    }

    private final DiffType diffType;
    private final int line;
    private final int length;

    public DiffAnnotation(DiffType type, int line, int length) {
        super(TYPE, false, null);
        this.diffType = type;
        this.line = line;
        this.length = length;
    }

    public Position getPosition() {
        return new Position(line, length);
    }

    public DiffType getDiffType() {
        return diffType;
    }

    @Override
    public int getLayer() {
        return 2;
    }

    @Override
    public void paint(GC gc, Canvas canvas, Rectangle bounds) {
        gc.setForeground(getDiffType().getColor());
        gc.drawString(getDiffType().getLabel(), bounds.x, bounds.y);
    }

}
