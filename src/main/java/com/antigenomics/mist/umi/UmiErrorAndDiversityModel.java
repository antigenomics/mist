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
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import org.apache.commons.math3.distribution.BinomialDistribution;

public class UmiErrorAndDiversityModel implements InputPort<UmiCoverageAndQuality> {
    private final static int MAX_UMI_LEN = 256;
    private final double[][] pwm = new double[MAX_UMI_LEN][4];
    private int total;

    public UmiErrorAndDiversityModel() {

    }

    public void put(UmiCoverageAndQuality umiCoverageAndQuality) {
        NucleotideSequence umi = umiCoverageAndQuality.getUmiTag().getSequence();
        int coverage = umiCoverageAndQuality.getCoverage();

        for (int i = 0; i < umi.size(); i++) {
            pwm[i][umi.codeAt(i)] += coverage;
        }

        total += coverage;
    }

    public void put(OutputPort<UmiCoverageAndQuality> umiInfoProvider) {
        UmiCoverageAndQuality umiCoverageAndQuality;

        while ((umiCoverageAndQuality = umiInfoProvider.take()) != null) {
            put(umiCoverageAndQuality);
        }
    }

    public double independentAssemblyProbability(NucleotideSequence parentUmi, NucleotideSequence childUmi) {
        double prob = 1.0;

        assert parentUmi.size() == childUmi.size();

        for (int i = 0; i < parentUmi.size(); i++) {
            byte code = parentUmi.codeAt(i);
            if (code == childUmi.codeAt(i)) {
                prob *= pwm[i][code];
                prob /= total;
            }
        }

        return prob;
    }

    public double independentAssemblyProbability(UmiCoverageAndQuality parent, UmiCoverageAndQuality child) {
        return independentAssemblyProbability(parent.getUmiTag().getSequence(), child.getUmiTag().getSequence());
    }

    public double errorPValue(UmiCoverageAndQuality parent, UmiCoverageAndQuality child) {
        double logProb = 0.0;

        NucleotideSequence parentUmi = parent.getUmiTag().getSequence(),
                childUmi = child.getUmiTag().getSequence();
        SequenceQuality childUmiQual = child.getQuality();

        for (int i = 0; i < parentUmi.size(); i++) {
            byte code = parentUmi.codeAt(i);
            if (code != childUmi.codeAt(i)) {
                logProb += childUmiQual.log10ProbabilityOfErrorAt(i);
            }
        }

        BinomialDistribution binomialDistribution = new BinomialDistribution(parent.getCoverage() + child.getCoverage(),
                Math.pow(10, logProb));

        return 1.0 - binomialDistribution.cumulativeProbability(child.getCoverage()) +
                0.5 * binomialDistribution.probability(child.getCoverage());
    }

    public double computeExpectedDiversity() {
        double entropy = 0;

        for (int i = 0; i < MAX_UMI_LEN; i++) {
            double partialEntropy = 0, sum = 0;
            for (int j = 0; j < 4; j++) {
                sum += pwm[i][j];
                partialEntropy += pwm[i][j] == 0 ? 0 : pwm[i][j] * Math.log10(pwm[i][j]);
            }
            if (sum > 0) {
                partialEntropy /= sum;
                partialEntropy -= Math.log10(sum);
            }
            entropy -= partialEntropy;
        }

        return Math.pow(10, entropy);
    }
}
