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
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class HeaderUtil {
    public static final String TOKEN_SEP = " ",
            FIELD_SEP = "_",
            PRIMER_ID_TOKEN = "PID" + FIELD_SEP,
            UMI_TOKEN = "UMI" + FIELD_SEP;

    public static String updateHeader(String description, PrimerSearcherResult primerSearcherResult) {
        String primerId = primerSearcherResult.getPrimerId();
        NSequenceWithQuality umiNSQ = primerSearcherResult.getLeftResult().getUmi()
                .concatenate(primerSearcherResult.getRightResult().getUmi());

        return description + TOKEN_SEP + PRIMER_ID_TOKEN +
                primerId +
                (umiNSQ.size() > 0 ?
                        TOKEN_SEP + UMI_TOKEN +
                                umiNSQ.getSequence().toString() +
                                FIELD_SEP +
                                umiNSQ.getQuality().toString() : "") +
                TOKEN_SEP;
    }

    public static String parsePrimerId(String description) {
        String[] tokens = description.split(PRIMER_ID_TOKEN);
        
        return tokens[1].split(TOKEN_SEP)[0];
    }

    public static NSequenceWithQuality parseUmiNSQ(String description) {
        String[] tokens = description.split(UMI_TOKEN);

        String umiString = tokens[1].split(TOKEN_SEP)[0];

        String[] seqAndQual = umiString.split(FIELD_SEP);

        return new NSequenceWithQuality(seqAndQual[0], seqAndQual[1]);
    }
}
