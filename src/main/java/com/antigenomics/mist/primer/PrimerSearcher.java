/*
 * Copyright 2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mist.primer;

import com.antigenomics.mist.preprocess.ReadWrapper;
import com.antigenomics.mist.primer.pattern.DummyPatternSearcher;
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

        if (patternSearcherLeft instanceof DummyPatternSearcher &&
                patternSearcherRight instanceof DummyPatternSearcher) {
            throw new IllegalArgumentException("Both left and right pattern searchers set to dummy.");
        }
    }

    public PrimerSearcherResult search(ReadWrapper readWrapper) {
        PatternSearchResult leftResult = patternSearcherLeft.searchFirst(readWrapper.getData(0, false)),
                rightResult = patternSearcherRight.searchLast(readWrapper.getData(1, false));

        if (reverseAllowed && (!leftResult.isMatching() || !rightResult.isMatching())) {
            leftResult = patternSearcherLeft.searchFirst(readWrapper.getData(0, true));
            rightResult = patternSearcherRight.searchLast(readWrapper.getData(1, true));
            if (leftResult.isMatching() && rightResult.isMatching()) {
                return new PrimerSearcherResult(leftResult, rightResult, primerId, readWrapper, true);
            }
        }

        return new PrimerSearcherResult(leftResult, rightResult, primerId, readWrapper, false);
    }
}
