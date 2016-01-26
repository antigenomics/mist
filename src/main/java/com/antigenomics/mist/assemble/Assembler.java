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

import cc.redberry.pipe.Processor;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Assembler<T extends SequenceRead> implements Processor<T, AssemblyResult<T>> {
    private final float minIdentity;
    private final int flankSize;

    public Assembler(float minIdentity, int flankSize) {
        this.minIdentity = minIdentity;
        this.flankSize = flankSize;
    }

    @Override
    public AssemblyResult<T> process(T input) {
        return null;
    }

    protected AssemblyPassResult assemble(List<T> reads, int index) {
        if (reads.isEmpty()) {
            return null;
        }

        List<T> sortedReads = new ArrayList<>(reads);

        sortedReads.sort((r1, r2) -> -Integer.compare(r1.getRead(index).getData().size(),
                r2.getRead(index).getData().size())); // sort reads from longest to shortest

        List<T> readsToAssemble = new ArrayList<>(), discardedReads = new ArrayList<>();
        List<Integer> offsets = new ArrayList<>();

        Iterator<T> iter = sortedReads.iterator();

        T core = iter.next();
        readsToAssemble.add(core);
        NucleotideSequence coreSeq = core.getRead(index).getData().getSequence();

        while (iter.hasNext()) {
            T read = iter.next();
            NucleotideSequence querySeq = read.getRead(index).getData().getSequence();

            int offset = querySeq.getRange(flankSize, querySeq.size() - flankSize)
                    .toMotif().getBitapPattern()
                    .mismatchAndIndelMatcherFirst((int) ((querySeq.size() - 2 * flankSize) * (1.0 - minIdentity)),
                            coreSeq)
                    .findNext();

            if (offset < 0) {
                discardedReads.add(read);
            } else {
                readsToAssemble.add(read);
                offsets.add(offset);
            }
        }

        return new AssemblyPassResult(readsToAssemble, discardedReads,
                assemble(readsToAssemble, offsets, discardedReads));
    }

    private NSequenceWithQuality assemble(List<T> readsToAssemble, List<Integer> offsets, List<T> discardedReads) {
        NSequenceWithQuality consensus;

        readsToAssemble.removeAll(discardedReads);

        return null;
    }

    private static int computeQualityMatch(byte q1, byte q2) {
        return q1 + q1;
    }

    private static int computeQualityMismatch(byte q1, byte q2) {
        return Math.min(Math.max(q1, q2), Math.max(Math.abs(q1 - q2), 3));
    }

    protected class AssemblyPassResult {
        private final List<T> assembledReads, discardedReads;
        private final NSequenceWithQuality consensus;

        public AssemblyPassResult(List<T> assembledReads,
                                  List<T> discardedReads,
                                  NSequenceWithQuality consensus) {
            this.assembledReads = assembledReads;
            this.discardedReads = discardedReads;
            this.consensus = consensus;
        }

        public List<T> getAssembledReads() {
            return assembledReads;
        }

        public List<T> getDiscardedReads() {
            return discardedReads;
        }

        public NSequenceWithQuality getConsensus() {
            return consensus;
        }
    }
}
