package com.antigenomics.mist.cli.barcodes;

import com.antigenomics.mist.primer.PrimerSearcher;
import com.antigenomics.mist.primer.PrimerSearcherArray;
import com.antigenomics.mist.primer.pattern.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class BarcodesParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static PrimerSearcherArrayModel getModel(PrimerSearcherArray primerSearcherArray) {
        return new PrimerSearcherArrayModel(
                new ArrayList<>(primerSearcherArray.getPrimerSearchers()
                        .stream()
                        .map(BarcodesParser::getModel)
                        .collect(Collectors.toList()))
        );
    }

    private static PrimerSearcherModel getModel(PrimerSearcher patternSearcher) {
        return new PrimerSearcherModel(patternSearcher.getPrimerId(),
                getModel(patternSearcher.getPatternSearcherLeft()),
                getModel(patternSearcher.getPatternSearcherRight()),
                patternSearcher.isReverseAllowed());
    }

    private static PatternSearcherModel getModel(PatternSearcher patternSearcher) {
        if (patternSearcher instanceof DummyPatternSearcher) {
            return new DummyPatternSearcherModel();
        }
        if (patternSearcher instanceof FuzzyPatternSearcher) {
            FuzzyPatternSearcher fuzzyPatternSearcher = (FuzzyPatternSearcher) patternSearcher;
            return new FuzzyPatternSearcherModel(fuzzyPatternSearcher.getPatternStr(),
                    fuzzyPatternSearcher.getMaxNumberOfErrors());
        }
        if (patternSearcher instanceof PositionalUmiExtractor) {
            PositionalUmiExtractor positionalUmiExtractor = (PositionalUmiExtractor) patternSearcher;
            return new PositionalUmiExtractorModel(positionalUmiExtractor.getFrom(),
                    positionalUmiExtractor.getTo());
        }
        if (patternSearcher instanceof StrictPatternMatcher) {
            StrictPatternMatcher strictPatternMatcher = (StrictPatternMatcher) patternSearcher;
            return new StrictPatternMatcherModel(strictPatternMatcher.getMinOffset(),
                    strictPatternMatcher.getMaxOffset(),
                    strictPatternMatcher.getPatternStr());
        }
        throw new NotImplementedException();

    }

    public static PrimerSearcherArray read(String json) throws IOException {
        return mapper.readValue(json, PrimerSearcherArrayModel.class).create();
    }

    public static String write(PrimerSearcherArray primerSearcherArray) throws IOException {
        return mapper.writeValueAsString(getModel(primerSearcherArray));
    }

    public static PrimerSearcherArray read(File json) throws IOException {
        return mapper.readValue(json, PrimerSearcherArrayModel.class).create();
    }

    public static void write(File json, PrimerSearcherArray primerSearcherArray) throws IOException {
        mapper.writeValue(json, getModel(primerSearcherArray));
    }
}
