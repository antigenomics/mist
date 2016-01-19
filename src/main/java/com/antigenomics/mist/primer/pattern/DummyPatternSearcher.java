package com.antigenomics.mist.primer.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;

public class DummyPatternSearcher implements PatternSearcher {
    @Override
    public PatternSearchResult searchFirst(NSequenceWithQuality read) {
        return PatternSearchResult.NO_SEARCH;
    }

    @Override
    public PatternSearchResult searchLast(NSequenceWithQuality read) {
        return PatternSearchResult.NO_SEARCH;
    }
}
