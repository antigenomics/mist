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

package com.antigenomics.mist.umi;

import com.milaboratory.core.sequence.NucleotideSequence;

public class UmiTag implements Comparable<UmiTag> {
    private final String primerId;
    private final NucleotideSequence sequence;

    public UmiTag(String primerId, NucleotideSequence sequence) {
        this.primerId = primerId;
        this.sequence = sequence;
    }

    public String getPrimerId() {
        return primerId;
    }

    public NucleotideSequence getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UmiTag umiTag = (UmiTag) o;

        return primerId.equals(umiTag.primerId) && sequence.equals(umiTag.sequence);
    }

    @Override
    public int hashCode() {
        return 31 * primerId.hashCode() + sequence.hashCode();
    }

    @Override
    public String toString() {
        return primerId + ":" + sequence.toString();
    }

    @Override
    public int compareTo(UmiTag o) {
        int res = primerId.compareTo(o.primerId);
        return res == 0 ? sequence.compareTo(o.sequence) : res;
    }
}
