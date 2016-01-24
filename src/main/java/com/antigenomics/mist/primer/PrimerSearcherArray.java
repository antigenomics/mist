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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongArray;

public class PrimerSearcherArray {
    private final List<PrimerSearcher> primerSearchers;
    private final AtomicLongArray leftMatchCounter, rightMatchCounter,
            bothMatchCounter, partialMatchCounter, reverseCounter;

    public PrimerSearcherArray(List<PrimerSearcher> primerSearchers) {
        this.primerSearchers = primerSearchers;
        this.leftMatchCounter = new AtomicLongArray(primerSearchers.size());
        this.rightMatchCounter = new AtomicLongArray(primerSearchers.size());
        this.bothMatchCounter = new AtomicLongArray(primerSearchers.size());
        this.partialMatchCounter = new AtomicLongArray(primerSearchers.size());
        this.reverseCounter = new AtomicLongArray(primerSearchers.size());

        if (primerSearchers.isEmpty()) {
            throw new IllegalArgumentException("Composite primer searcher list should be non-empty.");
        }
    }

    private void updateStats(int index, PrimerSearcherResult result) {
        boolean leftMatch = result.getLeftResult().isMatching(),
                rightMatch = result.getRightResult().isMatching();
        if (leftMatch || rightMatch) {
            if (leftMatch) {
                leftMatchCounter.incrementAndGet(index);
            }

            if (rightMatch) {
                rightMatchCounter.incrementAndGet(index);
            }

            if (leftMatch && rightMatch) {
                bothMatchCounter.incrementAndGet(index);
                if (result.isReversed()) {
                    reverseCounter.incrementAndGet(index);
                }
            } else {
                partialMatchCounter.incrementAndGet(index);
            }
        }
    }

    public List<PrimerSearcherStats> getStats() {
        List<PrimerSearcherStats> primerSearcherStatsList = new ArrayList<>();

        for (int i = 0; i < primerSearchers.size(); i++) {
            primerSearcherStatsList.add(new PrimerSearcherStats(
                    leftMatchCounter.get(i),
                    rightMatchCounter.get(i),
                    bothMatchCounter.get(i),
                    partialMatchCounter.get(i),
                    reverseCounter.get(i),
                    primerSearchers.get(i).getPrimerId()
            ));
        }

        return primerSearcherStatsList;
    }

    public PrimerSearcherResult search(ReadWrapper readWrapper) {
        PrimerSearcherResult bestResult = null;
        int bestResultIndex = -1;

        for (int i = 0; i < primerSearchers.size(); i++) {
            PrimerSearcher primerSearcher = primerSearchers.get(i);
            PrimerSearcherResult result = primerSearcher.search(readWrapper);

            if (bestResult == null ||
                    (bestResult.getLeftResult().getScore() <= result.getLeftResult().getScore() &&
                            bestResult.getRightResult().getScore() <= result.getRightResult().getScore())) {
                bestResult = result;
                bestResultIndex = i;
            }

            if (bestResult.getScore() == Byte.MAX_VALUE) {
                bestResultIndex = i;
                break;
            }
        }

        updateStats(bestResultIndex, bestResult);

        return bestResult;
    }

    public int size() {
        return primerSearchers.size();
    }
}
