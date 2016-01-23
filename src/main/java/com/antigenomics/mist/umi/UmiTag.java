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

public class UmiTag {
    private final String primerId;
    private final NucleotideSequence leftUmi, rightUmi;

    public UmiTag(String primerId, NucleotideSequence leftUmi, NucleotideSequence rightUmi) {
        this.primerId = primerId;
        this.leftUmi = leftUmi;
        this.rightUmi = rightUmi;
    }

    public String getPrimerId() {
        return primerId;
    }

    public NucleotideSequence getLeftUmi() {
        return leftUmi;
    }

    public NucleotideSequence getRightUmi() {
        return rightUmi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UmiTag umiTag = (UmiTag) o;

        if (!leftUmi.equals(umiTag.leftUmi)) return false;
        if (!primerId.equals(umiTag.primerId)) return false;
        if (!rightUmi.equals(umiTag.rightUmi)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = primerId.hashCode();
        result = 31 * result + leftUmi.hashCode();
        result = 31 * result + rightUmi.hashCode();
        return result;
    }
}
