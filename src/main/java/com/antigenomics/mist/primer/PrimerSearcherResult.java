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
