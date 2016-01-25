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
import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.core.sequence.NucleotideSequence;

public class UmiCorrectorSingle extends UmiCorrector<SingleRead> {
    public UmiCorrectorSingle(OutputPort<UmiCoverageAndQuality> input,
                              int maxMismatches, double errorLogOddsRatioThreshold) {
        super(input, maxMismatches, errorLogOddsRatioThreshold);
    }

    @Override
    public SingleRead process(SingleRead input) {
        HeaderUtil.ParsedHeader parsedHeader = HeaderUtil.parsedHeader(input.getDescription());
        UmiTag umiTag = parsedHeader.toUmiTag();

        // TODO: ignore reads with no UMI tags?
        // TODO: some stats

        UmiTree umiTree = umiTreeBySample.get(umiTag.getPrimerId());

        if (umiTree == null) {
            throw new IllegalArgumentException("The read is associated with a sample " +
                    "that is not present in UMI corrector.");
        }

        NucleotideSequence correctedUmi = umiTree.correct(umiTag.getSequence());

        if (!correctedUmi.equals(umiTag.getSequence())) {
            correctedCounter.incrementAndGet();
        }

        return new SingleReadImpl(input.getId(),
                input.getData(), HeaderUtil.updateHeader(parsedHeader.getRawDescription(),
                parsedHeader.getPrimerId(), correctedUmi));
    }
}
