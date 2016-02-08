package com.antigenomics.mist.umi;

import cc.redberry.pipe.InputPort;
import com.antigenomics.mist.misc.PoissonLogNormalEM;

// non thread-safe
public class UmiCoverageStatistics implements InputPort<UmiCoverageAndQuality> {
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

    @Override
    public void put(UmiCoverageAndQuality umiCoverageAndQuality) {
        if (umiCoverageAndQuality != null) {
            int coverage = umiCoverageAndQuality.getCoverage(),
                    bin = bin(coverage);

            weightedHistogram[bin] += coverage;
            histogram[bin]++;

            poissonLogNormalEM.update(coverage);

            total++;
            totalCoverage += coverage;
        }

        // TODO: summarize?
    }

    /**
     * TODO
     *
     * @return
     * @deprecated Use a more robust method to estimate threshold
     */
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

    /**
     * TODO
     *
     * @return
     * @deprecated Use a more robust method to estimate diversity
     */
    @Deprecated
    public int getObservedDiversityEstimate() {
        int observedDiversity = 0;
        for (int i = getThresholdEstimate(); i < numberOfBins; i++) {
            observedDiversity += histogram[i];
        }
        return observedDiversity;
    }

    public double getDensity(int umiCoverage) {
        return getCount(umiCoverage) / total;
    }

    public int getCount(int umiCoverage) {
        return histogram[bin(umiCoverage)];
    }

    public double getWeightedDensity(int umiCoverage) {
        return getWeightedCount(umiCoverage) / totalCoverage;
    }

    public long getWeightedCount(int umiCoverage) {
        return weightedHistogram[bin(umiCoverage)];
    }

    public PoissonLogNormalEM.PoissonLogNormalModel getWeightedDensityModel() {
        return poissonLogNormalEM.run(getThresholdEstimate());
    }

    public int getTotal() {
        return total;
    }

    @Override
    public String toString() {
        String str = "coverage\tweighted\tunweighted\testimate\n";

        int estimate = (int) (Math.log(getThresholdEstimate()) / Math.log(2));

        for (int i = 0; i <= Math.log(MAX_UMI_COVERAGE) / Math.log(2); i++) {
            int count = (int) Math.pow(2, i);

            str += count + "\t" + getWeightedCount(count) + "\t" + getCount(count) + "\t" +
                    (i == estimate ? "*" : " ") + "\n";
        }

        return str;
    }
}
