package com.antigenomics.mist.primer;

import com.antigenomics.mist.mig.Tag;
import com.antigenomics.mist.primer.pattern.PatternSearchResult;
import com.milaboratory.core.sequence.NucleotideSequence;

public class CompositePrimerSearcherResult implements Tag {
    private final PatternSearchResult leftResult, rightResult;
    private final String primerId;
    private final long readId;
    private final boolean reversed;

    public CompositePrimerSearcherResult(PatternSearchResult leftResult, PatternSearchResult rightResult,
                                         String primerId, long readId, boolean reversed) {
        this.leftResult = leftResult;
        this.rightResult = rightResult;
        this.reversed = reversed;
        this.primerId = primerId;
        this.readId = readId;
    }

    public PatternSearchResult getLeftResult() {
        return leftResult;
    }

    public PatternSearchResult getRightResult() {
        return rightResult;
    }

    public boolean isReversed() {
        return reversed;
    }

    public boolean isMatched() {
        return leftResult.isMatching() && rightResult.isMatching();
    }

    public byte getScore() {
        return leftResult.getScore() < rightResult.getScore() ? leftResult.getScore() : rightResult.getScore();
    }

    @Override
    public String getPrimerId() {
        return primerId;
    }

    @Override
    public NucleotideSequence getLeftUmi() {
        return leftResult.getUmi().getSequence();
    }

    @Override
    public NucleotideSequence getRightUmi() {
        return rightResult.getUmi().getSequence();
    }

    public long getReadId() {
        return readId;
    }
}
