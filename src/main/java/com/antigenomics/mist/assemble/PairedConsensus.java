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
    private final SingleConsensus consensus1, consensus2;
    private final int clusterId;
    private final List<PairedRead> reads;

    public PairedConsensus(SingleConsensus consensus1,
                           SingleConsensus consensus2,
                           int clusterId) {
        this.consensus1 = consensus1;
        this.consensus2 = consensus2;
        this.clusterId = clusterId;

        if (consensus1.size() < consensus2.size()) {
            consensus1 = consensus2;
            consensus2 = this.consensus1;
        }

        this.reads = consensus2.reads.stream()
                .map(read -> (PairedRead) read)
                .filter(consensus1.reads::contains)
                .collect(Collectors.toList());
    }

    public float getOverlap() {
        return reads.size() / (float) Math.min(consensus1.size(), consensus2.size());
    }

    @Override
    public List<PairedRead> getReads() {
        return reads;
    }

    @Override
    public int size(int index) {
        return (index > 0 ? consensus2 : consensus1).size();
    }

    @Override
    public PairedRead asRead() {
        return new PairedRead(consensus1.asRead(),
                consensus2.asRead());
    }

    public SingleConsensus getConsensus1() {
        return consensus1;
    }

    public SingleConsensus getConsensus2() {
        return consensus2;
    }

    @Override
    public UmiTag getUmiTag() {
        return consensus1.getUmiTag();
    }

    @Override
    public int getClusterId() {
        return clusterId;
    }
}
