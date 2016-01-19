package com.antigenomics.mist.primer;

import java.util.List;

public class SearcherArray {
    private final List<CompositePrimerSearcher> compositePrimerSearchers;

    public SearcherArray(List<CompositePrimerSearcher> compositePrimerSearchers) {
        this.compositePrimerSearchers = compositePrimerSearchers;

        if (compositePrimerSearchers.isEmpty()) {
            throw new IllegalArgumentException("Composite primer searcher list should be non-empty.");
        }
    }

    public CompositePrimerSearcherResult search(ReadWrapper readWrapper) {
        CompositePrimerSearcherResult bestResult = null;
        for (CompositePrimerSearcher compositePrimerSearcher : compositePrimerSearchers) {
            CompositePrimerSearcherResult result = compositePrimerSearcher.search(readWrapper);

            if (result.getScore() == Byte.MAX_VALUE) {
                return result;
            } else if (bestResult == null || bestResult.getScore() < result.getScore()) {
                bestResult = result;
            }
        }
        return bestResult;
    }
}
