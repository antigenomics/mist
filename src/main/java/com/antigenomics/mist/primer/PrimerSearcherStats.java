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

package com.antigenomics.mist.primer;


public class PrimerSearcherStats {
    private final long leftMatchCount, rightMatchCount,
            bothMatchCount, partialMatchCount,
            reverseCount;
    private final String primerId;

    public PrimerSearcherStats(long leftMatchCount, long rightMatchCount,
                               long bothMatchCount, long partialMatchCount,
                               long reverseCount, String primerId) {
        this.leftMatchCount = leftMatchCount;
        this.rightMatchCount = rightMatchCount;
        this.bothMatchCount = bothMatchCount;
        this.partialMatchCount = partialMatchCount;
        this.reverseCount = reverseCount;
        this.primerId = primerId;
    }

    public long getLeftMatchCount() {
        return leftMatchCount;
    }

    public long getRightMatchCount() {
        return rightMatchCount;
    }

    public long getBothMatchCount() {
        return bothMatchCount;
    }

    public long getPartialMatchCount() {
        return partialMatchCount;
    }

    public long getReverseCount() {
        return reverseCount;
    }

    public String getPrimerId() {
        return primerId;
    }

    public float getForwardMatchRatio() {
        return 1.0f - reverseCount / (float) bothMatchCount;
    }

    public float getPartialMatchRatio() {
        return partialMatchCount / (float) (partialMatchCount + bothMatchCount);
    }

    public float getPartialMatchLeftAsymmetry() {
        return (leftMatchCount - rightMatchCount) / (float) partialMatchCount;
    }
}
