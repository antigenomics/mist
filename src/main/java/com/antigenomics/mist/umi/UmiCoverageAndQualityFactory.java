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

import com.antigenomics.mist.misc.AtomicFloatArray;
import com.milaboratory.core.sequence.SequenceQuality;

import java.util.concurrent.atomic.AtomicInteger;

class UmiCoverageAndQualityFactory {
    private final UmiTag umiTag;
    private final AtomicInteger counter;
    private final AtomicFloatArray cumulativeQuality;

    public UmiCoverageAndQualityFactory(UmiTag umiTag, int length) {
        this.umiTag = umiTag;
        this.counter = new AtomicInteger();
        this.cumulativeQuality = new AtomicFloatArray(length);
    }

    public void update(SequenceQuality quality) {
        if (quality.size() != cumulativeQuality.length() ||
                quality.size() != cumulativeQuality.length()) {
            throw new IllegalArgumentException("UMI length mismatch.");
        }

        counter.incrementAndGet();

        for (int i = 0; i < quality.size(); i++) {
            cumulativeQuality.addAndGet(i, quality.value(i));
        }
    }

    public UmiCoverageAndQuality create() {
        byte[] data = new byte[cumulativeQuality.length()];
        int count = counter.get();

        for (int i = 0; i < cumulativeQuality.length(); i++) {
            data[i] = (byte) (cumulativeQuality.get(i) / count);
        }

        return new UmiCoverageAndQuality(umiTag, count, new SequenceQuality(data));
    }
}
