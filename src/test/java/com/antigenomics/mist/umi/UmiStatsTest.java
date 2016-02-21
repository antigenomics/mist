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

package com.antigenomics.mist.umi;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.DummyInputPort;
import com.antigenomics.mist.TestUtil;
import com.antigenomics.mist.misc.PoissonLogNormalEM;
import com.antigenomics.mist.preprocess.ReadWrapperFactory;
import com.antigenomics.mist.preprocess.SearchProcessor;
import com.antigenomics.mist.primer.PrimerSearcherArray;
import com.antigenomics.mist.primer.PrimerSearcherResult;
import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.fastq.PairedFastqReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class UmiStatsTest {
    @Test
    public void negativeThresholdTest() {
        double[] peakPositionValues = new double[]{1e-6, 1.0, 1.5};

        int i = 0;
        for (double peakPosition : peakPositionValues) {
            System.out.println("Synthetic UMI threshold test (negative) #" + (++i) + ":\n" +
                    "peakPosition=" + peakPosition);
            SyntheticUmiReadout syntheticUmiReadout = new SyntheticUmiReadout(1000,
                    12, (byte) 30, peakPosition, 0.5);

            UmiCoverageStatistics coverageStats = syntheticUmiReadout.getUmiCoverageStatistics();


            System.out.println(coverageStats.getThresholdEstimate());

            Assert.assertTrue(coverageStats.getModelThresholdEstimate() < 0);
            Assert.assertTrue(coverageStats.getThresholdEstimate() == 1.0);
        }
    }

    @Test
    public void thresholdTest() {
        double[] peakPositionValues = new double[]{3.0, 4.0, 5.0, 6.0};

        int i = 0;
        for (double peakPosition : peakPositionValues) {
            System.out.println("Synthetic UMI threshold test (positive) #" + (++i) + ":\n" +
                    "peakPosition=" + peakPosition);

            SyntheticUmiReadout syntheticUmiReadout = new SyntheticUmiReadout(1000,
                    12, (byte) 30, peakPosition, 1.0);

            UmiCoverageStatistics coverageStats = syntheticUmiReadout.getUmiCoverageStatistics();

            Assert.assertTrue(coverageStats.getSimpleThresholdEstimate() <= coverageStats.getThresholdEstimate());
            Assert.assertTrue(coverageStats.getThresholdEstimate() <= Math.pow(2, peakPosition - 1));
        }
    }

    @Test
    public void syntheticTest() {
        int[] numberOfUmisValues = new int[]{500, 1000, 5000};
        byte[] qualityValues = new byte[]{(byte) 25, (byte) 30, (byte) 35};
        double[] peakPositionValues = new double[]{4.0, 5.0, 6.0};

        int i = 0;
        for (int numberOfUmis : numberOfUmisValues) {
            for (byte quality : qualityValues) {
                for (double peakPosition : peakPositionValues) {
                    System.out.println("Synthetic UMI coverage statistic test#" + (++i) + ":\n" +
                            "numberOfUmis=" + numberOfUmis + "\n" +
                            "quality=" + quality + "\n" +
                            "peakPosition=" + peakPosition);

                    SyntheticUmiReadout syntheticUmiReadout = new SyntheticUmiReadout(numberOfUmis,
                            12, quality, peakPosition, 1.0);

                    UmiCoverageStatistics coverageStats = syntheticUmiReadout.getUmiCoverageStatistics();

                    testHistogramAndModelConsistency(coverageStats);

                    // Usually peak position is always lower than expected due to
                    // errors (peak parameter just specifies mean coverage, errors are applied after)
                    // Note that Mu parameter is for log-normal model, we need to adjust it as log2 is used in
                    // generator
                    Assert.assertTrue(Math.abs(peakPosition -
                            coverageStats.getWeightedDensityModel().getMu() / Math.log(2)) <
                            1.0);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void realDataTest() throws IOException, InterruptedException {
        System.out.println("Real data UMI coverage statistic test");

        PrimerSearcherArray primerSearcherArray = TestUtil.readBarcodes("umi_barcodes.txt");

        SearchProcessor searchProcessor = new SearchProcessor(
                new ReadWrapperFactory(false), // this is a sample from MIGEC/Checkout, reads already reversed
                primerSearcherArray);

        PairedFastqReader reader = new PairedFastqReader(
                TestUtil.resourceAsStream("umi_sample_R1.fastq.gz"),
                TestUtil.resourceAsStream("umi_sample_R2.fastq.gz")
        );

        ParallelProcessor<PairedRead, PrimerSearcherResult> processor = new ParallelProcessor(
                reader, searchProcessor, Runtime.getRuntime().availableProcessors()
        );

        CUtils.drain(processor, DummyInputPort.INSTANCE);

        UmiCoverageStatistics coverageStats = new UmiCoverageStatistics();

        CUtils.drain(searchProcessor.getUmiAccumulator().getOutputPort(), coverageStats);

        testHistogramAndModelConsistency(coverageStats);

        System.out.println(coverageStats.getModelThresholdEstimate());
        Assert.assertEquals(36, coverageStats.getThresholdEstimate());
    }

    private static void testHistogramAndModelConsistency(UmiCoverageStatistics coverageStats) {
        PoissonLogNormalEM.PoissonLogNormalModel densityModel = coverageStats.getWeightedDensityModel();

        System.out.println(coverageStats);
        System.out.println(densityModel);

        double unweightedError = 0, weightedError = 0,
                weightedSumHist = 0, weightedSumModel = 0;

        int i = 0;
        for (; i < UmiCoverageStatistics.MAX_UMI_COVERAGE; i++) {
            int x = (int) Math.pow(2, i);

            unweightedError += Math.abs(coverageStats.getDensity(x) -
                    densityModel.computeCoverageHistogramDensity(x));
            weightedError += Math.abs(coverageStats.getWeightedDensity(x) -
                    densityModel.computeCoverageHistogramDensityWeighted(x));

            weightedSumModel += densityModel.computeCoverageHistogramDensityWeighted(x);

            if ((weightedSumHist += coverageStats.getWeightedDensity(x)) > 0.95) {
                break;
            }
        }

        Assert.assertTrue(unweightedError / i < 0.2);
        Assert.assertTrue(weightedError / i < 0.05);
        Assert.assertTrue(weightedSumModel > 0.90);
    }
}
