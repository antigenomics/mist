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

package com.antigenomics.mist;

import com.antigenomics.mist.primer.PrimerSearcher;
import com.antigenomics.mist.primer.PrimerSearcherArray;
import com.antigenomics.mist.primer.pattern.DummyPatternSearcher;
import com.antigenomics.mist.primer.pattern.FuzzyPatternSearcher;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class TestUtil {
    public static RandomGenerator randomGenerator = new Well19937c(51102);
    public static final Random rnd = new Random(480011);

    public static InputStream resourceAsStream(String name) throws IOException {
        InputStream inputStream = TestUtil.class.getClassLoader().getResourceAsStream(name);

        return name.endsWith(".gz") ? new GZIPInputStream(inputStream) : inputStream;
    }

    public static PrimerSearcherArray readBarcodes(String resourceName) throws IOException {
        BufferedReader barcodesReader = new BufferedReader(new InputStreamReader(TestUtil.resourceAsStream(resourceName)));

        String line;

        List<PrimerSearcher> primerSearchers = new ArrayList<>();

        while ((line = barcodesReader.readLine()) != null) {
            String[] splitLine = line.split("\t");

            String master = splitLine[1], slave = splitLine.length > 2 ? splitLine[2] : ".";

            primerSearchers.add(new PrimerSearcher(splitLine[0],
                    barcodeExists(master) ? new FuzzyPatternSearcher(master, 3) : new DummyPatternSearcher(),
                    barcodeExists(slave) ? new FuzzyPatternSearcher(slave, 3) : new DummyPatternSearcher(),
                    true));
        }

        return new PrimerSearcherArray(primerSearchers);
    }

    private static boolean barcodeExists(String barcode) {
        return barcode.length() > 0 && !barcode.equals(".");
    }

    public static NucleotideSequence randomSequence(int length) {
        byte[] data = new byte[length];

        for (int i = 0; i < length; i++) {
            data[i] = (byte) rnd.nextInt(4);
        }

        return new NucleotideSequence(data);
    }

    public static SequenceQuality randomQuality(int length, byte mean) {
        byte[] qual = new byte[length];
        PoissonDistribution poissonDistribution = new PoissonDistribution(randomGenerator,
                Math.max(1, 40 - mean), PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);

        for (int i = 0; i < length; i++) {
            qual[i] = (byte) Math.min(Math.max(2, 40 - poissonDistribution.sample()), 40);
        }

        return new SequenceQuality(qual);
    }

    public static NSequenceWithQuality mutate(NSequenceWithQuality nSequenceWithQuality) {
        byte[] bases = new byte[nSequenceWithQuality.size()];

        for (int i = 0; i < nSequenceWithQuality.size(); i++) {
            if (rnd.nextDouble() > 4 * nSequenceWithQuality.getQuality().probabilityOfErrorAt(i) / 3) {
                bases[i] = nSequenceWithQuality.getSequence().codeAt(i);
            } else {
                bases[i] = (byte) rnd.nextInt(4);
            }
        }

        return new NSequenceWithQuality(new NucleotideSequence(bases), nSequenceWithQuality.getQuality());
    }
}
