package com.antigenomics.mist.primer.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;

public class PositionalUmiExtractor implements PatternSearcher {
    private final int from, to;

    public PositionalUmiExtractor(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public PatternSearchResult searchFirst(NSequenceWithQuality read) {
        return search(read, to, from);
    }

    @Override
    public PatternSearchResult searchLast(NSequenceWithQuality read) {
        return search(read, read.size() - to, read.size() - from);
    }

    private PatternSearchResult search(NSequenceWithQuality read, int from, int to) {
        return new PatternSearchResult(from, to, read.getRange(from, to));
    }
}
