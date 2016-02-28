package com.antigenomics.mist.cli.barcodes;

import com.antigenomics.mist.primer.pattern.PatternSearcher;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = DummyPatternSearcherModel.TYPE, value = DummyPatternSearcherModel.class),
        @JsonSubTypes.Type(name = FuzzyPatternSearcherModel.TYPE, value = FuzzyPatternSearcherModel.class),
        @JsonSubTypes.Type(name = PositionalUmiExtractorModel.TYPE, value = PositionalUmiExtractorModel.class),
        @JsonSubTypes.Type(name = StrictPatternMatcherModel.TYPE, value = StrictPatternMatcherModel.class)
})
public abstract class PatternSearcherModel {
    private String type;

    public PatternSearcherModel(String type) {
        this.type = type;
    }

    public abstract PatternSearcher create();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
