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

import com.antigenomics.mist.misc.HeaderUtil;
import com.antigenomics.mist.umi.UmiTag;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SingleConsensus implements Consensus<SingleRead> {
    private final Set<SequenceRead> reads;
    private final NSequenceWithQuality consensusNSQ;
    private final UmiTag umiTag;
    private final int index, clusterId;

    public SingleConsensus(Set<SequenceRead> reads, NSequenceWithQuality consensusNSQ,
                           int index, int clusterId, UmiTag umiTag) {
        this.reads = reads;
        this.consensusNSQ = consensusNSQ;
        this.index = index;
        this.clusterId = clusterId;
        this.umiTag = umiTag;
    }

    int readOverlap(SingleConsensus other) {
        Set<SequenceRead> set1, set2;

        if (size(index) < other.size(other.index)) {
            set1 = reads;
            set2 = other.reads;
        } else {
            set1 = other.reads;
            set2 = reads;
        }

        return (int) set1.stream().filter(set2::contains).count();
    }

    public NSequenceWithQuality getConsensusNSQ() {
        return consensusNSQ;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public UmiTag getUmiTag() {
        return umiTag;
    }

    @Override
    public int getClusterId() {
        return clusterId;
    }

    @Override
    public List<SingleRead> getReads() {
        return reads.stream().map(x -> x.getRead(index)).collect(Collectors.toList());
    }

    @Override
    public int size(int index) {
        return size();
    }

    public int size() {
        return reads.size();
    }

    @Override
    public SingleRead asRead() {
        return new SingleReadImpl(-1,
                consensusNSQ,
                HeaderUtil.updateHeader(HeaderUtil.CONSENSUS_HEADER_PREFIX, this));
    }
}
