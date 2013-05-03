package com.jedruch.eclipse.gitgutter;

import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

/**
 * @author Pawel Jedruch
 * 
 */
public class GitGutterRulerColumn extends LineNumberRulerColumn {

    private static final Color ADDED = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);

    @Override
    protected void paintLine(int line, int y, int lineheight, GC gc, Display display) {
        gc.setForeground(ADDED);
        super.paintLine(line, y, lineheight, gc, display);
    }

    @Override
    protected String createDisplayString(int line) {
        return "+";
    }

    @Override
    protected int computeNumberOfDigits() {
        return super.computeNumberOfDigits();
    }

    @Override
    protected Color getBackground(Display display) {
        return display.getSystemColor(SWT.COLOR_GRAY);
    }

}
