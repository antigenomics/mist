package com.antigenomics.mist.cli.barcodes;

import com.antigenomics.mist.primer.pattern.DummyPatternSearcher;
import com.antigenomics.mist.primer.pattern.PatternSearcher;

public class DummyPatternSearcherModel extends PatternSearcherModel{
    public static final String TYPE = "none";

    public DummyPatternSearcherModel() {
        super(TYPE);
    }

    @Override
    public PatternSearcher create() {
        return new DummyPatternSearcher();
    }
}
