package com.antigenomics.mist.primer;

import com.antigenomics.mist.preprocess.ReadWrapper;

import java.util.List;

public class PrimerSearcherArray {
    private final List<PrimerSearcher> primerSearchers;

    public PrimerSearcherArray(List<PrimerSearcher> primerSearchers) {
        this.primerSearchers = primerSearchers;

        if (primerSearchers.isEmpty()) {
            throw new IllegalArgumentException("Composite primer searcher list should be non-empty.");
        }
    }

    public PrimerSearcherResult search(ReadWrapper readWrapper) {
        PrimerSearcherResult bestResult = null;
        for (PrimerSearcher primerSearcher : primerSearchers) {
            PrimerSearcherResult result = primerSearcher.search(readWrapper);

            if (result.getScore() == Byte.MAX_VALUE) {
                return result;
            } else if (bestResult == null || bestResult.getScore() < result.getScore()) {
                bestResult = result;
            }
        }
        return bestResult;
    }
}
