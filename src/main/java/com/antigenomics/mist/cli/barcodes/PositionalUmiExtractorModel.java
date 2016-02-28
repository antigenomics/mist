package com.antigenomics.mist.cli.barcodes;

import com.antigenomics.mist.primer.pattern.PatternSearcher;
import com.antigenomics.mist.primer.pattern.PositionalUmiExtractor;

public class PositionalUmiExtractorModel extends PatternSearcherModel {
    public static final String TYPE = "positional";
    private int from, to;

    public PositionalUmiExtractorModel() {
        super(TYPE);
    }

    public PositionalUmiExtractorModel(int from, int to) {
        super(TYPE);
        this.from = from;
        this.to = to;
    }

    @Override
    public PatternSearcher create() {
        return new PositionalUmiExtractor(from, to);
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }
}
