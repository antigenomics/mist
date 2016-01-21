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

public class ReadWrapperFactory {
    private final boolean illuminaReads;

    public ReadWrapperFactory(boolean illuminaReads) {
        this.illuminaReads = illuminaReads;
    }

    public ReadWrapper wrap(SequenceRead read) {
        if (read.numberOfReads() == 1) {
            return new SingleReadWrapper(read);
        } else {
            return illuminaReads ? new PairedIlluminaReadWrapper(read) : new PairedReadWrapper(read);
        }
    }
}
