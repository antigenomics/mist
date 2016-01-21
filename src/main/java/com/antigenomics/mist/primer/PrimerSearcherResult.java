package com.antigenomics.mist.primer;

import com.antigenomics.mist.preprocess.ReadWrapper;
import com.antigenomics.mist.primer.pattern.PatternSearchResult;

public class PrimerSearcherResult {
    private final PatternSearchResult leftResult, rightResult;
    private final String primerId;
    private final ReadWrapper readWrapper;
    private final boolean reversed;

    public PrimerSearcherResult(PatternSearchResult leftResult, PatternSearchResult rightResult,
                                String primerId, ReadWrapper readWrapper, boolean reversed) {
        this.leftResult = leftResult;
        this.rightResult = rightResult;
        this.reversed = reversed;
        this.primerId = primerId;
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

    public String getPrimerId() {
        return primerId;
    }

    public ReadWrapper getReadWrapper() {
        return readWrapper;
    }
}
