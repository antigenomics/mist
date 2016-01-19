package com.antigenomics.mist.primer;

import com.antigenomics.mist.primer.pattern.PatternSearchResult;
import com.antigenomics.mist.primer.pattern.PatternSearcher;

public class CompositePrimerSearcher {
    private final String id;
    private final PatternSearcher patternSearcherLeft, patternSearcherRight;
    private final boolean reverseAllowed;

    public CompositePrimerSearcher(String id,
                                   PatternSearcher patternSearcherLeft,
                                   PatternSearcher patternSearcherRight,
                                   boolean reverseAllowed) {
        this.id = id;
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
            return new CompositePrimerSearcherResult(leftResult, rightResult, id, readWrapper, true);
        }

        return new CompositePrimerSearcherResult(leftResult, rightResult, id, readWrapper, false);
    }
}
