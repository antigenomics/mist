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

    protected final int maxMismatches, filterDecisionCoverageThreshold;
    protected final double densityModelErrorThreshold, errorPvalueThreshold, independentAssemblyFdrThreshold;

    public UmiCorrector(OutputPort<UmiCoverageAndQuality> input) {
        this(input, 10, 0.5, 1, 0.05, 0.1);
    }

    public UmiCorrector(OutputPort<UmiCoverageAndQuality> input,
                        int filterDecisionCoverageThreshold, double densityModelErrorThreshold,
                        int maxMismatches, double errorPvalueThreshold, double independentAssemblyFdrThreshold) {
        this.maxMismatches = maxMismatches;
        this.filterDecisionCoverageThreshold = filterDecisionCoverageThreshold;
        this.densityModelErrorThreshold = densityModelErrorThreshold;
        this.errorPvalueThreshold = errorPvalueThreshold;
        this.independentAssemblyFdrThreshold = independentAssemblyFdrThreshold;

        UmiCoverageAndQuality umiCoverageAndQuality;

        while ((umiCoverageAndQuality = input.take()) != null) {
            CorrectorStatistics correctorStatistics = correctorStatisticsBySample.computeIfAbsent(
                    umiCoverageAndQuality.getUmiTag().getPrimerId(),
                    tmp -> new CorrectorStatistics());

            correctorStatistics.put(umiCoverageAndQuality);
        }

        correctorStatisticsBySample.values().stream().forEach(CorrectorStatistics::summarize);
    }

    protected UmiTag correct(UmiTag umiTag) {
        String primerId = umiTag.getPrimerId();

        if (!correctorStatisticsBySample.containsKey(primerId)) {
            throw new IllegalArgumentException("The following sample was not found in corrector " + primerId);
        }

        return new UmiTag(primerId,
                correctorStatisticsBySample.get(primerId).correct(umiTag.getSequence()));
    }

    public long getCorrectedCount() {
        return correctedCounter.get();
    }

    protected final class CorrectorStatistics implements InputPort<UmiCoverageAndQuality> {
        private final UmiTree umiTree;
        private final UmiCoverageStatistics umiCoverageStatistics;
        private final boolean useCoverageThresholding;

        public CorrectorStatistics() {
            this.umiCoverageStatistics = new UmiCoverageStatistics();
            this.umiTree = new UmiTree(-1, maxMismatches,
                    errorPvalueThreshold, independentAssemblyFdrThreshold);
            this.useCoverageThresholding = umiCoverageStatistics.getThresholdEstimate() >= filterDecisionCoverageThreshold;
        }

        public NucleotideSequence correct(NucleotideSequence umi) {
            int coverage = umiTree.get(umi).getCoverage();

            if (useCoverageThresholding &&
                    umiCoverageStatistics
                            .getWeightedDensityModel()
                            .computeCoverageFilteringProbability(coverage) >= densityModelErrorThreshold) {
                return null;
            }

            return umiTree.correct(umi);
        }

        @Override
        public void put(UmiCoverageAndQuality umiCoverageAndQuality) {
            umiCoverageStatistics.put(umiCoverageAndQuality);
            umiTree.put(umiCoverageAndQuality);
        }

        public void summarize() {
            umiTree.setObservedDiversityEstimate(umiCoverageStatistics.getObservedDiversityEstimate());
            umiTree.traverseAndCorrect();
        }
    }
}
