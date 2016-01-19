package com.antigenomics.mist.primer.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;

public class PatternSearchResult {
    private final int from, to;
    private final byte score;
    private final NSequenceWithQuality umi;

    public static final PatternSearchResult NOT_FOUND = new PatternSearchResult(-1, -1, new NSequenceWithQuality("", ""), (byte) -1),
            NO_SEARCH = new PatternSearchResult(-1, -1);;

    public PatternSearchResult(int from, int to) {
        this(from, to, new NSequenceWithQuality("", ""));
    }

    public PatternSearchResult(int from, int to, NSequenceWithQuality umi) {
        this(from, to, umi, Byte.MAX_VALUE);
    }

    public PatternSearchResult(int from, int to, NSequenceWithQuality umi, byte score) {
        this.from = from;
        this.to = to;
        this.score = score;
        this.umi = umi;
    }

    public boolean isMatching() {
        return score >= 0;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public byte getScore() {
        return score;
    }

    public NSequenceWithQuality getUmi() {
        return umi;
    }
}
