package com.antigenomics.mist.cli.barcodes;

import com.antigenomics.mist.primer.pattern.FuzzyPatternSearcher;
import com.antigenomics.mist.primer.pattern.PatternSearcher;

public class FuzzyPatternSearcherModel extends PatternSearcherModel {
    public static final String TYPE = "fuzzy";
    private String pattern;
    private int maxNumberOfErrors;

    public FuzzyPatternSearcherModel() {
        super(TYPE);
    }

    public FuzzyPatternSearcherModel(String pattern, int maxNumberOfErrors) {
        super(TYPE);
        this.pattern = pattern;
        this.maxNumberOfErrors = maxNumberOfErrors;
    }

    @Override
    public PatternSearcher create() {
        return new FuzzyPatternSearcher(pattern, maxNumberOfErrors);
    }

    public String getPattern() {
        return pattern;
    }

    public int getMaxNumberOfErrors() {
        return maxNumberOfErrors;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setMaxNumberOfErrors(int maxNumberOfErrors) {
        this.maxNumberOfErrors = maxNumberOfErrors;
    }
}
