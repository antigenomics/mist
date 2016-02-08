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
import com.antigenomics.mist.preprocess.HeaderUtil;
import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;

public final class UmiCorrectorPaired extends UmiCorrector<PairedRead> {
    public UmiCorrectorPaired(OutputPort<UmiCoverageAndQuality> input) {
        super(input);
    }

    public UmiCorrectorPaired(OutputPort<UmiCoverageAndQuality> input,
                              int filterDecisionCoverageThreshold, double densityModelErrorThreshold,
                              int maxMismatches, double errorPvalueThreshold, double independentAssemblyFdrThreshold) {
        super(input,
                filterDecisionCoverageThreshold, densityModelErrorThreshold,
                maxMismatches, errorPvalueThreshold, independentAssemblyFdrThreshold);
    }

    @Override
    protected PairedRead replaceHeader(PairedRead read, String newDescription) {
        return new PairedRead(new SingleReadImpl(read.getId(),
                read.getR1().getData(), newDescription),
                new SingleReadImpl(read.getId(),
                        read.getR2().getData(), newDescription));
    }
}
