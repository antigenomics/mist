package com.antigenomics.mist.cli.barcodes;

import com.antigenomics.mist.primer.pattern.PatternSearcher;
import com.antigenomics.mist.primer.pattern.StrictPatternMatcher;

public class StrictPatternMatcherModel extends PatternSearcherModel {
    public static final String TYPE = "strict";
    private int minOffset, maxOffset;
    private String pattern;

    public StrictPatternMatcherModel() {
        super(TYPE);
    }

    public StrictPatternMatcherModel(int minOffset, int maxOffset, String pattern) {
        super(TYPE);
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
        this.pattern = pattern;
    }

    @Override
    public PatternSearcher create() {
        return new StrictPatternMatcher(minOffset, maxOffset, pattern);
    }

    public int getMinOffset() {
        return minOffset;
    }

    public int getMaxOffset() {
        return maxOffset;
    }

    public String getPattern() {
        return pattern;
    }

    public void setMinOffset(int minOffset) {
        this.minOffset = minOffset;
    }

    public void setMaxOffset(int maxOffset) {
        this.maxOffset = maxOffset;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
