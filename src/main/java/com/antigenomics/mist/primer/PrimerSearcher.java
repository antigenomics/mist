package com.antigenomics.mist.primer;

import com.antigenomics.mist.preprocess.ReadWrapper;
import com.antigenomics.mist.primer.pattern.PatternSearchResult;
import com.antigenomics.mist.primer.pattern.PatternSearcher;

public class PrimerSearcher {
    private final String primerId;
    private final PatternSearcher patternSearcherLeft, patternSearcherRight;
    private final boolean reverseAllowed;

    public PrimerSearcher(String primerId,
                          PatternSearcher patternSearcherLeft,
                          PatternSearcher patternSearcherRight,
                          boolean reverseAllowed) {
        this.primerId = primerId;
        this.patternSearcherLeft = patternSearcherLeft;
        this.patternSearcherRight = patternSearcherRight;
        this.reverseAllowed = reverseAllowed;
    }

    public PrimerSearcherResult search(ReadWrapper readWrapper) {
        PatternSearchResult leftResult = patternSearcherLeft.searchFirst(readWrapper.getData(0, false)),
                rightResult = patternSearcherRight.searchLast(readWrapper.getData(1, false));

        if (reverseAllowed && (!leftResult.isMatching() || !rightResult.isMatching())) {
            leftResult = patternSearcherLeft.searchFirst(readWrapper.getData(0, true));
            rightResult = patternSearcherRight.searchLast(readWrapper.getData(1, true));
            return new PrimerSearcherResult(leftResult, rightResult, primerId, readWrapper, true);
        }

        return new PrimerSearcherResult(leftResult, rightResult, primerId, readWrapper, false);
    }
}
