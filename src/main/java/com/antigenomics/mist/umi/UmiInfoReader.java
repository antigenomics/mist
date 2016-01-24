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
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UmiInfoReader implements OutputPort<UmiInfo> {
    private final BufferedReader reader;

    public UmiInfoReader(InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public UmiInfo take() {
        String line;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (line == null) {
            return null;
        }

        String[] splitLine = line.split("\t");

        return new UmiInfo(
                new UmiTag(
                        splitLine[0],
                        new NucleotideSequence(splitLine[1]),
                        new NucleotideSequence(splitLine[2])
                ),
                Integer.parseInt(splitLine[3]),
                new SequenceQuality(splitLine[4]),
                new SequenceQuality(splitLine[5]));
    }
}
