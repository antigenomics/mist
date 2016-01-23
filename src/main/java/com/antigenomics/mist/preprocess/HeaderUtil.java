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

import com.antigenomics.mist.primer.PrimerSearcherResult;
import com.antigenomics.mist.primer.pattern.PatternSearchResult;

public class HeaderUtil {
    public static final String TOKEN_SEP = " ",
            FIELD_SEP = "_",
            PRIMER_ID_TOKEN = "PID" + FIELD_SEP,
            LEFT_UMI_TOKEN = "MIL" + FIELD_SEP,
            RIGHT_UMI_TOKEN = "MIR" + FIELD_SEP;

    public static String generateHeader(PrimerSearcherResult primerSearcherResult) {
        String primerId = primerSearcherResult.getPrimerId();
        PatternSearchResult leftResult = primerSearcherResult.getLeftResult(),
                rightResult = primerSearcherResult.getRightResult();

        return TOKEN_SEP +
                PRIMER_ID_TOKEN + primerId +
                (leftResult.getUmi().size() > 0 ?
                        TOKEN_SEP + LEFT_UMI_TOKEN + leftResult.getUmi().getSequence().toString() +
                                FIELD_SEP + leftResult.getUmi().getQuality().toString() : "") +
                (rightResult.getUmi().size() > 0 ?
                        RIGHT_UMI_TOKEN + rightResult.getUmi().getSequence().toString() +
                                FIELD_SEP + rightResult.getUmi().getQuality().toString() : "") +
                TOKEN_SEP;
    }
}
