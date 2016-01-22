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

package com.antigenomics.mist.preprocess;

import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideAlphabet;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.Random;

public class ReadUtil {
    private static Random rnd = new Random(6585);

    public static NucleotideSequence removeNs(NucleotideSequence nucleotideSequence) {
        byte[] data = new byte[nucleotideSequence.size()];

        for (int i = 0; i < nucleotideSequence.size(); i++) {
            byte code = nucleotideSequence.codeAt(i);

            data[i] = code == NucleotideAlphabet.N ? (byte) rnd.nextInt(4) : code;
        }

        return new NucleotideSequence(data);
    }

    public static NSequenceWithQuality removeNs(NSequenceWithQuality nSequenceWithQuality) {
        return new NSequenceWithQuality(removeNs(nSequenceWithQuality.getSequence()),
                nSequenceWithQuality.getQuality());
    }

    public static SingleRead removeNs(SingleRead singleRead) {
        return new SingleReadImpl(singleRead.getId(),
                removeNs(singleRead.getData()),
                singleRead.getDescription());
    }
    
    public static PairedRead removeNs(PairedRead pairedRead) {
        return new PairedRead(removeNs(pairedRead.getR1()),
                removeNs(pairedRead.getR2()));
    }
}
