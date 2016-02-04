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

import java.util.HashMap;
import java.util.Map;

public class UmiStatistics {
    private final Map<String, UmiCoverageStatistics> umiCoverageModelBySample = new HashMap<>();

    public UmiStatistics() {
    }

    public void update(UmiCoverageAndQuality umiCoverageAndQuality){
        umiCoverageModelBySample
                .computeIfAbsent(umiCoverageAndQuality.getUmiTag().getPrimerId(), tmp -> new UmiCoverageStatistics())
                .update(umiCoverageAndQuality);
    }

    public void update(OutputPort<UmiCoverageAndQuality> umiInfoProvider) {
        UmiCoverageAndQuality umiCoverageAndQuality;

        while ((umiCoverageAndQuality = umiInfoProvider.take()) != null) {
            update(umiCoverageAndQuality);
        }
    }

    /**
     * Returns an empty model if sample does not exist
     *
     * @param sampleId
     * @return
     */
    public UmiCoverageStatistics getUmiCoverageStatistics(String sampleId) {
        return umiCoverageModelBySample.computeIfAbsent(sampleId, tmp -> new UmiCoverageStatistics());
    }
}
