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

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UmiAccumulator {
    private final Map<UmiTag, UmiCoverageAndQualityFactory> umiInfoFactoryMap = new ConcurrentHashMap<>();

    public UmiAccumulator() {
    }

    public void put(NSequenceWithQuality leftUmi, NSequenceWithQuality rightUmi) {
        put(UmiTag.DEFAULT_PRIMER_ID, leftUmi, rightUmi);
    }

    public void put(String primerId, NSequenceWithQuality leftUmi, NSequenceWithQuality rightUmi) {
        put(primerId, leftUmi.concatenate(rightUmi));
    }

    public void put(NSequenceWithQuality umiNSQ) {
        put(UmiTag.DEFAULT_PRIMER_ID, umiNSQ);
    }

    public void put(String primerId, NSequenceWithQuality umiNSQ) {
        UmiTag umiTag = new UmiTag(primerId, umiNSQ.getSequence());

        UmiCoverageAndQualityFactory umiCoverageAndQualityFactory = umiInfoFactoryMap.computeIfAbsent(umiTag,
                tmp -> new UmiCoverageAndQualityFactory(umiTag, umiNSQ.size()));

        umiCoverageAndQualityFactory.append(umiNSQ.getQuality());
    }

    public UmiCoverageAndQuality getAt(UmiTag umiTag) {
        if (!umiInfoFactoryMap.containsKey(umiTag)) {
            throw new IllegalArgumentException("Tag " + umiTag + " is not in the accumulator.");
        }

        return umiInfoFactoryMap.get(umiTag).create();
    }

    public int size() {
        return umiInfoFactoryMap.size();
    }

    public OutputPort<UmiCoverageAndQuality> getOutputPort() {
        return new UmiCoverageAndQualityOutputPort();
    }

    private class UmiCoverageAndQualityOutputPort implements OutputPort<UmiCoverageAndQuality> {
        private final Iterator<UmiCoverageAndQualityFactory> iter;

        public UmiCoverageAndQualityOutputPort() {
            this.iter = umiInfoFactoryMap.values().iterator();
        }

        @Override
        public UmiCoverageAndQuality take() {
            if (!iter.hasNext()) {
                return null;
            }

            return iter.next().create();
        }
    }
}
