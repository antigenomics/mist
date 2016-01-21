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

import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class SingleReadWrapper implements ReadWrapper {
    private final SequenceRead read;

    public SingleReadWrapper(SequenceRead read) {
        this.read = read;
    }

    @Override
    public SequenceRead getRead() {
        return read;
    }

    @Override
    public NSequenceWithQuality getData(int index, boolean reverse) {
        if (index > 1 || index < 0)
            throw new IndexOutOfBoundsException("Allowed indexes for read wrapper are 0 and 1.");
        return reverse ? read.getRead(0).getData().getReverseComplement() : read.getRead(0).getData();
    }
}
