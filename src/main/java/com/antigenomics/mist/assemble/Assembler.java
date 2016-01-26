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
import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.List;

public class Assembler<T extends SequenceRead> implements Processor<T, AssemblyResult<T>> {
    @Override
    public AssemblyResult<T> process(T input) {
        return null;
    }

    protected AssemblyPassResult assemble(List<T> reads, int index) {
        Map<NucleotideSequence, Integer>
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
