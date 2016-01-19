package com.antigenomics.mist.primer.pattern;

import com.milaboratory.core.motif.Motif;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.List;

public class StrictPatternMatcher implements PatternSearcher {
    private final int minOffset, maxOffset;
    private final List<Integer> umiPositions;
    private final Motif<NucleotideSequence> pattern;

    public StrictPatternMatcher(int minOffset, int maxOffset, String pattern) {
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
        this.umiPositions = PatternSearcherUtil.extractUmiPositions(pattern);
        this.pattern = new NucleotideSequence(pattern.toUpperCase()).toMotif();

        if (minOffset > 0) {
            throw new RuntimeException("Minimal offset should be non-positive.");
        }
        if (maxOffset < 0) {
            throw new RuntimeException("Maximal offset should be non-negative.");
        }
    }

    @Override
    public PatternSearchResult searchFirst(NSequenceWithQuality read) {
        PatternSearchResult patternSearchResult = search(read, 0, 0);
        if (patternSearchResult != PatternSearchResult.NOT_FOUND) {
            return patternSearchResult;
        }

        for (int offset = 1; offset <= maxOffset; offset++) {
            patternSearchResult = search(read, offset, 0);
            if (patternSearchResult != PatternSearchResult.NOT_FOUND) {
                return patternSearchResult;
            }
        }

        for (int offset = minOffset; offset < 0; offset++) {
            patternSearchResult = search(read, 0, -offset);
            if (patternSearchResult != PatternSearchResult.NOT_FOUND) {
                return patternSearchResult;
            }
        }

        return PatternSearchResult.NOT_FOUND;
    }

    @Override
    public PatternSearchResult searchLast(NSequenceWithQuality read) {
        PatternSearchResult patternSearchResult = search(read, 0, 0);
        if (patternSearchResult != PatternSearchResult.NOT_FOUND) {
            return patternSearchResult;
        }

        int readAnchor = read.size() - pattern.size();

        for (int offset = minOffset; offset < 0; offset++) {
            patternSearchResult = search(read, readAnchor + offset, 0);
            if (patternSearchResult != PatternSearchResult.NOT_FOUND) {
                return patternSearchResult;
            }
        }

        for (int offset = 1; offset <= maxOffset; offset++) {
            patternSearchResult = search(read, readAnchor + offset, 0);
            if (patternSearchResult != PatternSearchResult.NOT_FOUND) {
                return patternSearchResult;
            }
        }

        return PatternSearchResult.NOT_FOUND;
    }

    private PatternSearchResult search(NSequenceWithQuality read, int fromRead, int fromPattern) {
        int posRead = fromRead, posPattern = fromPattern;

        for (; posPattern < pattern.size() && posRead < read.size(); posPattern++, posRead++) {
            if (!pattern.allows(read.getSequence().codeAt(posRead), posPattern)) {
                return PatternSearchResult.NOT_FOUND;
            }
        }

        return new PatternSearchResult(fromRead, posRead,
                PatternSearcherUtil.extractUmi(umiPositions, read, fromRead - fromPattern));
    }
}
