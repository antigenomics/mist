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
import com.milaboratory.core.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.alignment.Aligner;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;

import java.util.ArrayList;
import java.util.List;

public abstract class Assembler<T extends SequenceRead> implements Processor<Mig<T>, AssemblyResult<T>> {
    public static final int QUAL_SCALE_FACTOR = 10, MAX_CONSEQUENT_MISMATCHES = 3;

    private final float minSimilarity, maxDiscardedReadsRatio;
    private final int minAlignmentSize, maxAssemblePasses;

    public Assembler() {
        this(0.8f, 30, 0.7f, 3);
    }

    public Assembler(float minSimilarity, int minAlignmentSize, float maxDiscardedReadsRatio, int maxAssemblePasses) {
        // todo: check argument ranges
        this.minSimilarity = minSimilarity;
        this.minAlignmentSize = minAlignmentSize;
        this.maxDiscardedReadsRatio = maxDiscardedReadsRatio;
        this.maxAssemblePasses = maxAssemblePasses;
    }

    protected List<AssemblyPassResult> assemble(List<T> reads, int index) {
        List<AssemblyPassResult> results = new ArrayList<>();

        AssemblyPassResult previousResult = null;

        for (int i = 0; i < maxAssemblePasses; i++) {
            AssemblyPassResult result = assemblePass(previousResult != null ? previousResult.discardedReads : reads,
                    index);

            if (result != null && result.isGood()) {
                results.add(result);
                previousResult = result;
            } else {
                break;
            }
        }

        return results;
    }

    protected AssemblyPassResult assemblePass(List<T> reads) {
        return assemblePass(reads, 0);
    }

    protected AssemblyPassResult assemblePass(List<T> reads, int index) {
        if (reads.isEmpty()) {
            return null;
        }

        // Build the consensus by majority vote

        int size = 0;

        for (T read : reads) {
            size = Math.max(size, read.getRead(index).getData().size());
        }

        ConsensusPwm consensusPwm = new ConsensusPwm(size, index);

        reads.forEach(consensusPwm::append);

        // Re-align all reads

        NucleotideSequence consensusSequence = consensusPwm.create();
        int[] quality = new int[consensusSequence.size()];
        List<T> assembledReads = new ArrayList<>(),
                discardedReads = new ArrayList<>();

        for (T read : reads) {
            NSequenceWithQuality nsq = read.getRead(index).getData();
            int[] qualityTemp = new int[consensusSequence.size()];
            int mismatches = 0, consequentMismatches = 0;

            // Try to align without indels and update quality

            int offset = index > 0 ? (size - nsq.size()) : 0; // anchor left/right
            for (int i = 0; i < nsq.size(); i++) {
                if (consensusSequence.codeAt(offset + i) == nsq.getSequence().codeAt(i)) {
                    consequentMismatches = 0;
                    qualityTemp[i] = computeQualityMatch(quality[i], nsq.getQuality().value(i));
                } else {
                    mismatches++;
                    if (++consequentMismatches == MAX_CONSEQUENT_MISMATCHES) {
                        break; // apparently, we've got some indels
                    }
                    qualityTemp[i] = computeQualityMismatch(quality[i], nsq.getQuality().value(i));
                }
            }

            int alignmentSize;

            // Handle indels appropriately

            if (consequentMismatches == MAX_CONSEQUENT_MISMATCHES) {
                // Seems we have indels here.. try local alignment
                Alignment alignment = Aligner.alignLocalAffine(AffineGapAlignmentScoring.getNucleotideBLASTScoring(),
                        nsq.getSequence(), consensusSequence);

                // Cleanup
                mismatches = 0;
                qualityTemp = new int[consensusSequence.size()];

                // Traverse through aligned positions
                int prevPosInCons = Integer.MIN_VALUE;
                for (int pos = alignment.getSequence1Range().getFrom();
                     pos < alignment.getSequence1Range().getTo(); pos++) {
                    int posInCons = alignment.convertPosition(pos);

                    if (posInCons >= 0) {
                        if (nsq.getSequence().codeAt(pos) == consensusSequence.codeAt(posInCons)) {
                            qualityTemp[posInCons] = computeQualityMatch(quality[posInCons],
                                    nsq.getQuality().value(pos));
                        } else if (posInCons == prevPosInCons) { // count insertions only once
                            mismatches++;
                            qualityTemp[posInCons] = computeQualityMismatch(quality[posInCons],
                                    nsq.getQuality().value(pos));
                        }
                    } else if (posInCons == prevPosInCons) { // count deletions only once
                        mismatches++;
                    }
                    prevPosInCons = posInCons;
                }
                alignmentSize = alignment.getSequence1Range().length();
            } else {
                alignmentSize = nsq.size();
            }

            // Check if we want to keep a given read

            if (alignmentSize >= minAlignmentSize && mismatches / (float) alignmentSize >= minSimilarity) {
                assembledReads.add(read);
                quality = qualityTemp;
            } else {
                discardedReads.add(read);
            }
        }

        byte[] qualityBytes = new byte[consensusSequence.size()];
        for (int i = 0; i < quality.length; i++) {
            qualityBytes[i] = (byte) Math.min(Byte.MAX_VALUE, quality[i] / QUAL_SCALE_FACTOR);
        }

        return new AssemblyPassResult(assembledReads, discardedReads,
                new NSequenceWithQuality(consensusSequence, new SequenceQuality(qualityBytes)));
    }

    private static int computeQualityMatch(int q1, byte q2) {
        return q1 + q2;
    }

    private static int computeQualityMismatch(int q1, byte q2) {
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

        public boolean isGood() {
            return discardedReads.size() / (float) (assembledReads.size() + discardedReads.size()) <= maxDiscardedReadsRatio;
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

    private class ConsensusPwm {
        private final long[][] pwm;
        private final int size;
        private final int index;

        public ConsensusPwm(int size, int index) {
            this.size = size;
            this.pwm = new long[4][size];
            this.index = index;
        }

        public void append(T read) {
            append(read.getRead(index).getData());
        }

        public void append(NSequenceWithQuality nsq) {
            int offset = index > 0 ? (size - nsq.size()) : 0; // anchor left/right
            for (int i = 0; i < nsq.size(); i++) {
                pwm[nsq.getSequence().codeAt(i)][offset + i] += nsq.getQuality().value(i);
            }
        }

        public NucleotideSequence create() {
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++) {
                byte maxLetter = 0;
                long maxScore = 0, score;

                for (byte j = 0; j < 4; j++) {
                    if ((score = pwm[j][i]) > maxScore) {
                        maxScore = score;
                        maxLetter = j;
                    }
                }

                data[i] = maxLetter;
            }
            return new NucleotideSequence(data);
        }
    }
}
