package com.antigenomics.mist.umi;

import com.antigenomics.mist.misc.PoissonLogNormalEM;

// non thread-safe
public class UmiCoverageStatistics {
    public static final int MAX_UMI_COVERAGE = 65536;
    private final long[] weightedHistogram;
    private final int[] histogram;
    private final int numberOfBins;
    private final PoissonLogNormalEM poissonLogNormalEM;
    private int total, totalCoverage;

    public UmiCoverageStatistics() {
        this(MAX_UMI_COVERAGE);
    }

    public UmiCoverageStatistics(int maxUmiCoverage) {
        this.poissonLogNormalEM = new PoissonLogNormalEM();
        this.numberOfBins = (int) (Math.log(maxUmiCoverage) / Math.log(2)) + 1;
        this.weightedHistogram = new long[numberOfBins];
        this.histogram = new int[numberOfBins];
        this.total = 0;
        this.totalCoverage = 0;
    }

    private int bin(long x) {
        return Math.min(numberOfBins - 1, (int) (Math.log(x) / Math.log(2)));
    }

    public void update(UmiCoverageAndQuality umiCoverageAndQuality) {
        int coverage = umiCoverageAndQuality.getCoverage(),
                bin = bin(coverage);

        weightedHistogram[bin] += coverage;
        histogram[bin]++;

        poissonLogNormalEM.update(coverage);

        total++;
        totalCoverage += coverage;
    }

    public int getThresholdEstimate() {
        int indexOfMax = -1;
        long maxValue = -1;

        for (int i = 0; i < numberOfBins; i++) {
            if (weightedHistogram[i] > maxValue) {
                indexOfMax = i;
                maxValue = weightedHistogram[i];
            }
        }

        return (int) Math.pow(2, indexOfMax / 2);
    }

    public double getDensity(int umiCoverage) {
        return getCount(umiCoverage) / total;
    }

    public double getCount(int umiCoverage) {
        return histogram[bin(umiCoverage)];
    }

    public double getWeightedDensity(int umiCoverage) {
        return getWeightedCount(umiCoverage) / totalCoverage;
    }

    public double getWeightedCount(int umiCoverage) {
        return weightedHistogram[bin(umiCoverage)];
    }

    public PoissonLogNormalEM.PoissonLogNormalModel getWeightedDensityModel() {
        return poissonLogNormalEM.run(getThresholdEstimate());
    }

    public int getTotal() {
        return total;
    }
}
