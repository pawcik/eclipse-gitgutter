package com.jedruch.eclipse.gitgutter.ui.annotation;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

public class DiffAnnotation extends Annotation {

	
	public DiffAnnotation() {
		super("com.jedruch.eclipse.gitgutter.diff", false, null);
		setText("test");
	}

	public Position getPosition() {
		return new Position(0, 1);
	}
	

}
