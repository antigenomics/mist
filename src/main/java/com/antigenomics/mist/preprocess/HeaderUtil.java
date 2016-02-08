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
import com.antigenomics.mist.umi.UmiTag;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;

public class HeaderUtil {
    public static final String TOKEN_SEP = " ",
            FIELD_SEP = "_",
            PRIMER_ID_TOKEN = "PID" + FIELD_SEP,
            UMI_TOKEN = "UMI" + FIELD_SEP,
            LOW_COVERAGE_TAG = "LOW_COV";

    public static String updateHeader(String description, PrimerSearcherResult primerSearcherResult) {
        // TODO: check no tokens are present in description

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

    public static String updateHeaderLowCov(String description) {
        return description + TOKEN_SEP + LOW_COVERAGE_TAG;
    }

    public static String updateHeader(String description, UmiTag umiTag) {
        return updateHeader(description, umiTag.getPrimerId(), umiTag.getSequence());
    }

    public static String updateHeader(String description, String primerId, NucleotideSequence umi) {
        return description + TOKEN_SEP + PRIMER_ID_TOKEN +
                primerId +
                TOKEN_SEP + UMI_TOKEN +
                umi.toString() +
                TOKEN_SEP;
    }

    public static ParsedHeader parsedHeader(String description) {
        return new ParsedHeader(description);
    }

    public static ParsedHeader parsedHeader(SequenceRead read) {
        return new ParsedHeader(read.getRead(0).getDescription());
    }

    public static class ParsedHeader {
        private String rawDescription, primerId;
        private NucleotideSequence umiSeq;
        private SequenceQuality umiQual;

        private ParsedHeader(String description) {
            String[] tokens = description.split(PRIMER_ID_TOKEN);

            this.rawDescription = tokens[0];

            if (tokens.length > 1) {
                primerId = tokens[1].split(TOKEN_SEP)[0];

                tokens = tokens[1].split(UMI_TOKEN);

                if (tokens.length > 1) {
                    String umiString = tokens[1].split(TOKEN_SEP)[0];

                    String[] seqAndQual = umiString.split(FIELD_SEP);

                    this.umiSeq = new NucleotideSequence(seqAndQual[0]);
                    if (seqAndQual.length > 1) {
                        this.umiQual = new SequenceQuality(seqAndQual[1]);
                    } else {
                        this.umiQual = null;
                    }
                } else {
                    this.umiSeq = null;
                    this.umiQual = null;
                }
            } else {
                this.primerId = null;
                this.umiSeq = null;
                this.umiQual = null;
            }
        }

        public String getRawDescription() {
            return rawDescription;
        }

        public String getPrimerId() {
            return primerId;
        }

        public NucleotideSequence getUmiSeq() {
            return umiSeq;
        }

        public SequenceQuality getUmiQual() {
            return umiQual;
        }

        public UmiTag toUmiTag() {
            if (primerId == null || umiSeq == null) {
                throw new RuntimeException("Cannot convert header to UmiTag: some critical fields are missing.");
            }

            return new UmiTag(primerId, umiSeq);
        }
    }
}
