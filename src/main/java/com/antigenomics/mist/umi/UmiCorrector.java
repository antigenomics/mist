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
import cc.redberry.pipe.Processor;
import com.milaboratory.core.io.sequence.SequenceRead;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class UmiCorrector<T extends SequenceRead> implements Processor<T, T> {
    protected final Map<String, UmiTree> umiTreeBySample = new HashMap<>();
    protected final AtomicLong correctedCounter = new AtomicLong();

    public UmiCorrector(OutputPort<UmiCoverageAndQuality> input,
                         int maxMismatches,
                        double errorPvalueThreshold, double independentAssemblyFdrThreshold) {
        UmiCoverageAndQuality umiCoverageAndQuality;

        // TODO: UMI histogram stuff goes here!
        // TODO: in process() - if failed to find a parent using probabilistic model filter if below threshold


        int numberOfUmis = -1;

        while ((umiCoverageAndQuality = input.take()) != null) {
            UmiTree umiTree = umiTreeBySample.computeIfAbsent(umiCoverageAndQuality.getUmiTag().getPrimerId(),
                    tmp -> new UmiTree(numberOfUmis, maxMismatches,
                            errorPvalueThreshold, independentAssemblyFdrThreshold));

            umiTree.update(umiCoverageAndQuality);
        }
    }

    public UmiCoverageAndQuality get(UmiTag umiTag) {
        return umiTreeBySample.get(umiTag.getPrimerId()).get(umiTag.getSequence());
    }

    public long getCorrectedCount() {
        return correctedCounter.get();
    }
}
