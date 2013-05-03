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

public class DiffAnnotation extends Annotation implements
		IAnnotationPresentation {

	private static final String TYPE = "com.jedruch.eclipse.gitgutter.diff";

	public enum DiffType {
		ADDED("+", Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN)), DELETED(
				"▬", Display.getCurrent().getSystemColor(SWT.COLOR_RED)), MODIFIED(
				"▄", Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA));

		private final String label;
		private Color color;

		DiffType(String label, Color c) {
			this.label = label;
			this.color = c;
		}

		public String getLabel() {
			return label;
		}

		public Color getColor() {
			return color;
		}

	}

	private DiffType diffType;
	private int line;

	public DiffAnnotation(DiffType type, int line) {
		super(TYPE, false, null);
		this.diffType = type;
		this.line = line;
	}

	public Position getPosition() {
		return new Position(line * 10, line);
	}

	public DiffType getDiffType() {
		return diffType;
	}

	@Override
	public int getLayer() {
		return 0;
	}

	@Override
	public void paint(GC gc, Canvas canvas, Rectangle bounds) {
		gc.setForeground(getDiffType().getColor());
		gc.drawString(getDiffType().getLabel(), bounds.x, bounds.y);
	}

}
