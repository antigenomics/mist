package com.antigenomics.mist.primer;

import com.antigenomics.mist.primer.pattern.PatternSearchResult;

public class CompositePrimerSearcherResult {
    private final PatternSearchResult leftResult, rightResult;
    private final boolean reversed;

    public CompositePrimerSearcherResult(PatternSearchResult leftResult, PatternSearchResult rightResult,
                                         boolean reversed) {
        this.leftResult = leftResult;
        this.rightResult = rightResult;
        this.reversed = reversed;
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
}
