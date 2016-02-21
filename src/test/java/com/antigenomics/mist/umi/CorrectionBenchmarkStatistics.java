package com.antigenomics.mist.umi;

public class CorrectionBenchmarkStatistics implements Comparable<CorrectionBenchmarkStatistics> {
    private final int totalErrors, correctedErrors, weakCorrectedErrors,
            totalGood, miscorrectedGood;

    public CorrectionBenchmarkStatistics(int totalErrors, int correctedErrors, int weakCorrectedErrors,
                                         int totalGood, int miscorrectedGood) {
        this.totalErrors = totalErrors;
        this.correctedErrors = correctedErrors;
        this.weakCorrectedErrors = weakCorrectedErrors;
        this.totalGood = totalGood;
        this.miscorrectedGood = miscorrectedGood;
    }

    public double getTruePositiveRate() {
        return correctedErrors / (double) totalErrors;
    }

    public double getWeakTruePositiveRate() {
        return (weakCorrectedErrors + correctedErrors) / (double) totalErrors;
    }

    public double getFalsePositiveRate() {
        return miscorrectedGood / (double) totalGood;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public int getCorrectedErrors() {
        return correctedErrors;
    }

    public int getWeakCorrectedErrors() {
        return weakCorrectedErrors;
    }

    public int getTotalGood() {
        return totalGood;
    }

    public int getMiscorrectedGood() {
        return miscorrectedGood;
    }

    @Override
    public String toString() {
        return "TP=" + (float) getTruePositiveRate() +
                ", TP*=" + (float) getWeakTruePositiveRate() +
                ", FP=" + (float) getFalsePositiveRate();
    }

    @Override
    public int compareTo(CorrectionBenchmarkStatistics o) {
        int res = Double.compare(getTruePositiveRate(), o.getTruePositiveRate());
        return res == 0 ? -Double.compare(getFalsePositiveRate(), o.getFalsePositiveRate()) : res;
    }
}