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

public class UmiInfo {
    private final UmiTag umiTag;
    private final long count;
    private final SequenceQuality averageQualityLeft, averageQualityRight;

    public UmiInfo(UmiTag umiTag, long count,
                   SequenceQuality averageQualityLeft, SequenceQuality averageQualityRight) {
        this.umiTag = umiTag;
        this.count = count;
        this.averageQualityLeft = averageQualityLeft;
        this.averageQualityRight = averageQualityRight;
    }

    public UmiTag getUmiTag() {
        return umiTag;
    }

    public long getCount() {
        return count;
    }

    public SequenceQuality getAverageQualityLeft() {
        return averageQualityLeft;
    }

    public SequenceQuality getAverageQualityRight() {
        return averageQualityRight;
    }
}
