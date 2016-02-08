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

package com.antigenomics.mist.assemble;

import com.antigenomics.mist.umi.UmiTag;
import com.milaboratory.core.io.sequence.PairedRead;

import java.util.List;
import java.util.stream.Collectors;

public class PairedConsensus implements Consensus<PairedRead> {
    private final SingleConsensus leftConsensus, rightConsensus;
    private final int clusterId;
    private final List<PairedRead> reads;

    public PairedConsensus(SingleConsensus leftConsensus,
                           SingleConsensus rightConsensus,
                           int clusterId) {
        this.leftConsensus = leftConsensus;
        this.rightConsensus = rightConsensus;
        this.clusterId = clusterId;

        if (leftConsensus.size() < rightConsensus.size()) {
            leftConsensus = rightConsensus;
            rightConsensus = this.leftConsensus;
        }

        this.reads = rightConsensus.reads.stream()
                .map(read -> (PairedRead) read)
                .filter(leftConsensus.reads::contains)
                .collect(Collectors.toList());
    }

    public float getOverlap() {
        return reads.size() / (float) Math.min(leftConsensus.size(), rightConsensus.size());
    }

    @Override
    public List<PairedRead> getReads() {
        return reads;
    }

    @Override
    public int size(int index) {
        return (index > 0 ? rightConsensus : leftConsensus).size();
    }

    @Override
    public PairedRead asRead() {
        return new PairedRead(leftConsensus.asRead(),
                rightConsensus.asRead());
    }

    @Override
    public UmiTag getUmiTag() {
        return leftConsensus.getUmiTag();
    }

    @Override
    public int getClusterId() {
        return clusterId;
    }
}
