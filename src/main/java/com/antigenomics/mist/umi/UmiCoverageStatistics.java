package com.antigenomics.mist.umi;

import com.antigenomics.mist.misc.PoissonLogNormalEM;

// non thread-safe
public class UmiCoverageStatistics {
    private final long[] weightedHistogram;
    private final int[] histogram;
    private final int numberOfBins;
    private final PoissonLogNormalEM poissonLogNormalEM;
    private int total;

    public UmiCoverageStatistics() {
        this(65536);
    }

    public UmiCoverageStatistics(int maxCount) {
        this.poissonLogNormalEM = new PoissonLogNormalEM();
        this.numberOfBins = (int) (Math.log(maxCount) / Math.log(2)) + 1;
        this.weightedHistogram = new long[numberOfBins];
        this.histogram = new int[numberOfBins];
        this.total = 0;
    }

    private int bin(long x) {
        return Math.min(numberOfBins - 1, (int) (Math.log(x) / Math.log(2)));
    }

    public void update(UmiCoverageAndQuality umiCoverageAndQuality) {
        int bin = bin(umiCoverageAndQuality.getCoverage());

        weightedHistogram[bin] += umiCoverageAndQuality.getCoverage();
        histogram[bin]++;

        poissonLogNormalEM.update(umiCoverageAndQuality.getCoverage());

        total++;
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

    public double getWeighted(int umiCoverage) {
        return getWeightedCount(umiCoverage) / total;
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
