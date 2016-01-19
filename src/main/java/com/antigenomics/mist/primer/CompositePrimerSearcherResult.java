package com.antigenomics.mist.primer;

import com.antigenomics.mist.primer.pattern.PatternSearchResult;

public class CompositePrimerSearcherResult {
    private final PatternSearchResult leftResult, rightResult;
    private final String id;
    private final ReadWrapper readWrapper;
    private final boolean reversed;

    public CompositePrimerSearcherResult(PatternSearchResult leftResult, PatternSearchResult rightResult,
                                         String id, ReadWrapper readWrapper, boolean reversed) {
        this.leftResult = leftResult;
        this.rightResult = rightResult;
        this.reversed = reversed;
        this.id = id;
        this.readWrapper = readWrapper;
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

    public String getId() {
        return id;
    }

    public ReadWrapper getReadWrapper() {
        return readWrapper;
    }
}
