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

import java.util.concurrent.atomic.AtomicLong;

public class UmiInfoFactory {
    private final UmiTag umiTag;
    private final AtomicLong counter;
    private final AtomicFloatArray qualitySumLeft, qualitySumRight;

    public UmiInfoFactory(UmiTag umiTag, int lengthLeft, int lengthRight) {
        this.umiTag = umiTag;
        this.counter = new AtomicLong();
        this.qualitySumLeft = new AtomicFloatArray(lengthLeft);
        this.qualitySumRight = new AtomicFloatArray(lengthRight);
    }

    public void update(SequenceQuality qualityLeft, SequenceQuality qualityRight) {
        if (qualityLeft.size() != qualitySumLeft.length() ||
                qualityRight.size() != qualitySumRight.length()) {
            throw new IllegalArgumentException("UMI length mismatch.");
        }

        counter.incrementAndGet();

        for (int i = 0; i < qualityLeft.size(); i++) {
            qualitySumLeft.addAndGet(i, qualityLeft.value(i));
        }

        for (int i = 0; i < qualityRight.size(); i++) {
            qualitySumRight.addAndGet(i, qualityRight.value(i));
        }
    }

    public UmiInfo create() {
        byte[] dataLeft = new byte[qualitySumLeft.length()],
                dataRight = new byte[qualitySumRight.length()];
        long count = counter.get();

        for (int i = 0; i < qualitySumLeft.length(); i++) {
            dataLeft[i] = (byte) (qualitySumLeft.get(i) / count);
        }

        for (int i = 0; i < qualitySumRight.length(); i++) {
            dataRight[i] = (byte) (qualitySumRight.get(i) / count);
        }

        return new UmiInfo(umiTag, count, new SequenceQuality(dataLeft), new SequenceQuality(dataRight));
    }
}
