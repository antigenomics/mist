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

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import org.apache.commons.math3.distribution.BinomialDistribution;

public class UmiErrorAndDiversityModel {
    private final static int MAX_UMI_LEN = 256;
    private final double[][] pwm = new double[MAX_UMI_LEN][4];
    private int total;

    public UmiErrorAndDiversityModel() {


    }

    public void update(UmiCoverageAndQuality umiCoverageAndQuality) {
        NucleotideSequence umi = umiCoverageAndQuality.getUmiTag().getSequence();
        int coverage = umiCoverageAndQuality.getCoverage();

        for (int i = 0; i < umi.size(); i++) {
            pwm[i][umi.codeAt(i)] += coverage;
        }

        total += coverage;
    }

    public double getErrorLogOddsRatio(UmiCoverageAndQuality parent, UmiCoverageAndQuality child) {
        return Math.log10(errorProbability(parent, child)) -
                Math.log10(independentAssemblyProbability(parent, child));

    }

    public double independentAssemblyProbability(UmiCoverageAndQuality parent, UmiCoverageAndQuality child) {
        double prob = 1.0;

        NucleotideSequence parentUmi = parent.getUmiTag().getSequence(),
                childUmi = child.getUmiTag().getSequence();

        for (int i = 0; i < parentUmi.size(); i++) {
            byte code = parentUmi.codeAt(i);
            if (code == childUmi.codeAt(i)) {
                prob *= pwm[i][code];
                prob /= total;
            }
        }

        return prob;
    }

    public double errorProbability(UmiCoverageAndQuality parent, UmiCoverageAndQuality child) {
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

        return new BinomialDistribution(parent.getCoverage() + child.getCoverage(), Math.pow(10, logProb))
                .cumulativeProbability(child.getCoverage());
    }
}
