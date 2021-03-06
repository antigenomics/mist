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

import com.milaboratory.core.sequence.SequenceQuality;

public class UmiCoverageAndQuality {
    private final UmiTag umiTag;
    private final int coverage;
    private final SequenceQuality quality;
    private UmiTag parent;

    public UmiCoverageAndQuality(UmiTag umiTag, int coverage, SequenceQuality quality) {
        this.umiTag = umiTag;
        this.coverage = coverage;
        this.quality = quality;
    }

    public UmiTag getUmiTag() {
        return umiTag;
    }

    public int getCoverage() {
        return coverage;
    }

    public SequenceQuality getQuality() {
        return quality;
    }

    public UmiTag getParent() {
        return parent;
    }

    public void setParent(UmiTag parent) {
        this.parent = parent;
    }
}
