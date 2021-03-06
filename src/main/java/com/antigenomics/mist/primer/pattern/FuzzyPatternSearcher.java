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

package com.antigenomics.mist.primer.pattern;

import com.milaboratory.core.motif.BitapMatcher;
import com.milaboratory.core.motif.BitapPattern;
import com.milaboratory.core.motif.Motif;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.List;

public class FuzzyPatternSearcher implements PatternSearcher {
    private final Motif motif;
    private final BitapPattern pattern;
    private final List<Integer> seedPositions, umiPositions;
    private final int maxNumberOfErrors;

    public FuzzyPatternSearcher(String pattern, int maxNumberOfErrors) {
        this.maxNumberOfErrors = maxNumberOfErrors;
        this.seedPositions = PatternSearcherUtil.extractSeedPositions(pattern);
        this.umiPositions = PatternSearcherUtil.extractUmiPositions(pattern);
        this.motif = new NucleotideSequence(pattern.toUpperCase()).toMotif();
        this.pattern = motif.getBitapPattern();
    }

    @Override
    public PatternSearchResult searchFirst(NSequenceWithQuality read) {
        BitapMatcher matcher = pattern.substitutionOnlyMatcherFirst(maxNumberOfErrors, read.getSequence());
        return search(read, matcher.findNext(), matcher.getNumberOfErrors());
    }

    @Override
    public PatternSearchResult searchLast(NSequenceWithQuality read) {
        BitapMatcher matcher = pattern.substitutionOnlyMatcherFirst(maxNumberOfErrors, read.getSequence());
        int readFrom = matcher.findNext(), numberOfErrors = matcher.getNumberOfErrors(),
                nextReadFrom;

        while ((nextReadFrom = matcher.findNext()) >= 0) {
            readFrom = nextReadFrom;
            numberOfErrors = matcher.getNumberOfErrors();
        }

        return search(read, readFrom, numberOfErrors);
    }

    private PatternSearchResult search(NSequenceWithQuality read, int readFrom, int numberOfErrors) {
        if (readFrom < 0) {
            return PatternSearchResult.NOT_FOUND;
        } else {
            for (int seedPosition : seedPositions) {
                if (!motif.allows(read.getSequence().codeAt(readFrom + seedPosition), seedPosition)) {
                    return PatternSearchResult.NOT_FOUND;
                }
            }
            return new PatternSearchResult(readFrom, readFrom + motif.size(),
                    PatternSearcherUtil.extractUmi(umiPositions, read, readFrom),
                    (byte) (Byte.MAX_VALUE * ((maxNumberOfErrors - numberOfErrors) / (float) maxNumberOfErrors))
            );
        }
    }
}