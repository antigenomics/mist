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

import java.util.List;

public class PrimerSearcherArray {
    private final List<PrimerSearcher> primerSearchers;

    public PrimerSearcherArray(List<PrimerSearcher> primerSearchers) {
        this.primerSearchers = primerSearchers;

        if (primerSearchers.isEmpty()) {
            throw new IllegalArgumentException("Composite primer searcher list should be non-empty.");
        }
    }

    public PrimerSearcherResult search(ReadWrapper readWrapper) {
        PrimerSearcherResult bestResult = null;
        for (PrimerSearcher primerSearcher : primerSearchers) {
            PrimerSearcherResult result = primerSearcher.search(readWrapper);

            if (result.getScore() == Byte.MAX_VALUE) {
                return result;
            } else if (bestResult == null || bestResult.getScore() < result.getScore()) {
                bestResult = result;
            }
        }
        return bestResult;
    }
}
