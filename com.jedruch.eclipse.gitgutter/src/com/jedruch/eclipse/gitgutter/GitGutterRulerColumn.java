package com.jedruch.eclipse.gitgutter;

import org.eclipse.jface.text.source.LineNumberRulerColumn;

/**
 * @author Pawel Jedruch
 * 
 */
public class GitGutterRulerColumn extends LineNumberRulerColumn {

    @Override
    protected String createDisplayString(int line) {
        return "x" + super.createDisplayString(line);
    }

    @Override
    protected int computeNumberOfDigits() {
        return super.computeNumberOfDigits() + 1;
    }

}
