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

public class UmiSetInfo {
    private final Map<UmiTag, UmiInfoFactory> umiInfoFactoryMap = new ConcurrentHashMap<>();

    public UmiSetInfo() {
    }

    public void update(String primerId,
                       NSequenceWithQuality leftUmi, NSequenceWithQuality rightUmi) {
        UmiTag umiTag = new UmiTag(primerId, leftUmi.getSequence(), rightUmi.getSequence());

        UmiInfoFactory umiInfoFactory = umiInfoFactoryMap.computeIfAbsent(umiTag,
                tmp -> new UmiInfoFactory(umiTag, leftUmi.size(), rightUmi.size()));

        umiInfoFactory.update(leftUmi.getQuality(), rightUmi.getQuality());
    }

    public OutputPort<UmiInfo> getUmiInfoProvider() {
        return new UmiInfoProvider();
    }

    private class UmiInfoProvider implements OutputPort<UmiInfo> {
        private final Iterator<UmiInfoFactory> iter;

        public UmiInfoProvider() {
            this.iter = umiInfoFactoryMap.values().iterator();
        }

        @Override
        public UmiInfo take() {
            if (!iter.hasNext()) {
                return null;
            }

            return iter.next().create();
        }
    }
}
