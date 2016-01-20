package com.antigenomics.mist.primer;

import com.antigenomics.mist.primer.pattern.PatternSearchResult;
import com.antigenomics.mist.primer.pattern.PatternSearcher;

public class CompositePrimerSearcher {
    private final String primerId;
    private final PatternSearcher patternSearcherLeft, patternSearcherRight;
    private final boolean reverseAllowed;

    public CompositePrimerSearcher(String primerId,
                                   PatternSearcher patternSearcherLeft,
                                   PatternSearcher patternSearcherRight,
                                   boolean reverseAllowed) {
        this.primerId = primerId;
        this.patternSearcherLeft = patternSearcherLeft;
        this.patternSearcherRight = patternSearcherRight;
        this.reverseAllowed = reverseAllowed;
    }

    public CompositePrimerSearcherResult search(ReadWrapper readWrapper) {
        PatternSearchResult leftResult = patternSearcherLeft.searchFirst(readWrapper.getData(0, false)),
                rightResult = patternSearcherRight.searchLast(readWrapper.getData(1, false));

        if (reverseAllowed && (!leftResult.isMatching() || !rightResult.isMatching())) {
            leftResult = patternSearcherLeft.searchFirst(readWrapper.getData(0, true));
            rightResult = patternSearcherRight.searchLast(readWrapper.getData(1, true));
            return new CompositePrimerSearcherResult(leftResult, rightResult, primerId, readWrapper.getRead().getId(), true);
        }

        return new CompositePrimerSearcherResult(leftResult, rightResult, primerId, readWrapper.getRead().getId(), false);
    }
}
