package com.antigenomics.mist.umi;

// non thread-safe
public class UmiStatisticsBySample {
    private final long[] weightedHistogram;
    private final int[] histogram;
    private final int numberOfBins;
    //private final PoissonLogNormalEM model;

    public UmiStatisticsBySample(int threshold) {
        this.numberOfBins = (int) (Math.log(threshold) / Math.log(2)) + 1;
        this.weightedHistogram = new long[numberOfBins];
        this.histogram = new int[numberOfBins];
    }

    private int bin(long x) {
        return Math.min(numberOfBins - 1, (int) (Math.log(x) / Math.log(2)));
    }

    public void update(UmiInfo umiInfo) {
        int bin = bin(umiInfo.getCount());

        weightedHistogram[bin] += umiInfo.getCount();
        histogram[bin]++;
    }

    public int estimateThreshold() {
        int indexOfMax = -1;
        long maxValue = -1;

        for (int i = 0; i < numberOfBins; i++) {
            if (weightedHistogram[i] > maxValue) {
                indexOfMax = i;
                maxValue = weightedHistogram[i];
            }
        }

        return (int) Math.pow(2, indexOfMax);
    }
}
