package com.antigenomics.mist.primer.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideAlphabet;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;

import java.util.ArrayList;
import java.util.List;

public class PatternSearcherUtil {
    public static final char UMI_BASE = 'N';

    public static List<Integer> extractUmiPositions(String pattern) {
        List<Integer> umiPositions = new ArrayList<>();

        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == UMI_BASE) {
                umiPositions.add(i);
            }
        }

        return umiPositions;
    }

    public static List<Integer> extractSeedPositions(String pattern) {
        List<Integer> seedPositions = new ArrayList<>();

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (Character.isUpperCase(c) && c != UMI_BASE) {
                seedPositions.add(i);
            }
        }

        return seedPositions;
    }

    public static NSequenceWithQuality extractUmi(List<Integer> umiPositions, NSequenceWithQuality read, int offset) {
        byte[] seq = new byte[umiPositions.size()], qual = new byte[umiPositions.size()];

        for (int i = 0; i < umiPositions.size(); i++) {
            int pos = umiPositions.get(i) + offset;

            if (pos >= 0 && pos < read.size()) {
                seq[i] = read.getSequence().codeAt(pos);
                qual[i] = read.getQuality().value(pos);
            } else {
                // Handle UMI bases that fall from read range
                seq[i] = NucleotideAlphabet.N;
                qual[i] = SequenceQuality.BAD_QUALITY_VALUE;
            }
        }

        return new NSequenceWithQuality(new NucleotideSequence(seq), new SequenceQuality(qual));
    }
}
