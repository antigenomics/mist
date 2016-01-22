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
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class PairedIlluminaReadWrapper implements ReadWrapper {
    private final PairedRead read;
    private NSequenceWithQuality[] cache = new NSequenceWithQuality[4];

    public PairedIlluminaReadWrapper(PairedRead read) {
        this.read = read;
        cache[0] = read.getR1().getData();
        cache[1] = read.getR2().getData().getReverseComplement();
    }

    @Override
    public SequenceRead getRead() {
        return read;
    }

    @Override
    public NSequenceWithQuality getData(int index, boolean reversed) {
        if (reversed && cache[2] == null) {
            cache[2] = read.getR2().getData();
            cache[3] = read.getR1().getData().getReverseComplement();
        }
        return cache[reversed ? 2 + index : index];
    }
}
