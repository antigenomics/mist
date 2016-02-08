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

import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.Processor;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class UmiCorrector<T extends SequenceRead> implements Processor<T, T> {
    protected final Map<String, CorrectorStatistics> correctorStatisticsBySample = new HashMap<>();
    protected final AtomicLong correctedCounter = new AtomicLong();

    protected final int maxMismatches;
    protected double errorPvalueThreshold, independentAssemblyFdrThreshold;

    public UmiCorrector(OutputPort<UmiCoverageAndQuality> input,
                        int maxMismatches, double errorPvalueThreshold, double independentAssemblyFdrThreshold) {
        this.maxMismatches = maxMismatches;
        this.errorPvalueThreshold = errorPvalueThreshold;
        this.independentAssemblyFdrThreshold = independentAssemblyFdrThreshold;

        UmiCoverageAndQuality umiCoverageAndQuality;

        // TODO: UMI histogram stuff goes here!
        // TODO: in process() - if failed to find a parent using probabilistic model filter if below threshold

        while ((umiCoverageAndQuality = input.take()) != null) {
            CorrectorStatistics correctorStatistics = correctorStatisticsBySample.computeIfAbsent(
                    umiCoverageAndQuality.getUmiTag().getPrimerId(),
                    tmp -> new CorrectorStatistics());

            correctorStatistics.put(umiCoverageAndQuality);
        }

        correctorStatisticsBySample.values().stream().forEach(CorrectorStatistics::summarize);
    }

    protected UmiCoverageAndQuality get(UmiTag umiTag) {
        return correctorStatisticsBySample.get(umiTag.getPrimerId()).get(umiTag.getSequence());
    }

    public long getCorrectedCount() {
        return correctedCounter.get();
    }

    protected final class CorrectorStatistics implements InputPort<UmiCoverageAndQuality>{
        private final UmiTree umiTree;
        private final UmiCoverageStatistics umiCoverageStatistics;

        public CorrectorStatistics() {
            this.umiCoverageStatistics = new UmiCoverageStatistics();
            this.umiTree = new UmiTree(-1, maxMismatches,
                    errorPvalueThreshold, independentAssemblyFdrThreshold);
        }

        public UmiCoverageAndQuality get(NucleotideSequence umi){
           return umiTree.get(umi);
        }

        @Override
        public void put(UmiCoverageAndQuality umiCoverageAndQuality) {
            umiCoverageStatistics.put(umiCoverageAndQuality);
            umiTree.put(umiCoverageAndQuality);
        }

        public void summarize(){
            umiTree.setObservedDiversityEstimate(umiCoverageStatistics.getObservedDiversityEstimate());
            umiTree.traverseAndCorrect();
        }
    }
}
