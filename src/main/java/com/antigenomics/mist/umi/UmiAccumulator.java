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

    public void update(String primerId, NSequenceWithQuality leftUmi, NSequenceWithQuality rightUmi) {

        NSequenceWithQuality umiNSQ = leftUmi.concatenate(rightUmi);

        UmiTag umiTag = new UmiTag(primerId, umiNSQ.getSequence());

        UmiCoverageAndQualityFactory umiCoverageAndQualityFactory = umiInfoFactoryMap.computeIfAbsent(umiTag,
                tmp -> new UmiCoverageAndQualityFactory(umiTag, umiNSQ.size()));

        umiCoverageAndQualityFactory.update(umiNSQ.getQuality());
    }

    public OutputPort<UmiCoverageAndQuality> getUmiInfoProvider() {
        return new UmiInfoProvider();
    }

    private class UmiInfoProvider implements OutputPort<UmiCoverageAndQuality> {
        private final Iterator<UmiCoverageAndQualityFactory> iter;

        public UmiInfoProvider() {
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
