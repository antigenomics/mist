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

import com.milaboratory.core.sequence.NSequenceWithQuality;

public class PatternSearchResult {
    private final int from, to;
    private final byte score;
    private final NSequenceWithQuality umi;

    private static final NSequenceWithQuality EMPTY_NSQ = new NSequenceWithQuality("", "");

    public static final PatternSearchResult NOT_FOUND = new PatternSearchResult(-1, -1, EMPTY_NSQ, (byte) -1),
            NO_SEARCH = new PatternSearchResult(-1, -1);

    public PatternSearchResult(int from, int to) {
        this(from, to, EMPTY_NSQ);
    }

    public PatternSearchResult(int from, int to, NSequenceWithQuality umi) {
        this(from, to, umi, Byte.MAX_VALUE);
    }

    public PatternSearchResult(int from, int to, NSequenceWithQuality umi, byte score) {
        this.from = from;
        this.to = to;
        this.score = score;
        this.umi = umi;
    }

    public boolean isMatching() {
        return score >= 0;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public byte getScore() {
        return score;
    }

    public NSequenceWithQuality getUmi() {
        return umi;
    }
}
