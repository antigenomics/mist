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
    public void syntheticTest() {
        SyntheticUmiStats syntheticUmiStats = new SyntheticUmiStats(1000, 12, (byte) 35, 5.0, 1.0);

        UmiCoverageStatistics coverageStats = syntheticUmiStats.getUmiCoverageStatistics();

        System.out.println(coverageStats);

        Assert.assertEquals((int) (syntheticUmiStats.getLog2CoverageMean() - 1), coverageStats.getThresholdEstimate());

        PoissonLogNormalEM.PoissonLogNormalModel densityModel = coverageStats.getWeightedDensityModel();

        Assert.assertTrue(Math.abs(densityModel.getLambda() -
                Math.pow(10, -syntheticUmiStats.getMeanQual() / 10d) *
                        syntheticUmiStats.getUmiLength() * Math.pow(2, syntheticUmiStats.getLog2CoverageMean())) < 0.03);
        Assert.assertTrue(Math.abs(densityModel.getMu() - syntheticUmiStats.getLog2CoverageMean()) < 1.5);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void realDataTest() throws IOException, InterruptedException {
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

        while (processor.take() != null) {

        }

        UmiCoverageStatistics coverageStats = new UmiCoverageStatistics();

        CUtils.drain(searchProcessor.getUmiAccumulator().getOutputPort(), coverageStats);

        System.out.println("threshold=" + coverageStats.getThresholdEstimate());

        PoissonLogNormalEM.PoissonLogNormalModel densityModel = coverageStats.getWeightedDensityModel();

        double unweightedError = 0, weightedError = 0,
                weightedSumHist = 0, weightedSumModel = 0;

        int i = 0;
        for (; i < UmiCoverageStatistics.MAX_UMI_COVERAGE; i++) {
            int x = (int) Math.pow(2, i);
            
            /*
            System.out.println(x + "\t" +
                            coverageStats.getDensity(x) + "\t" + coverageStats.getWeightedDensity(x) + "\t" +
                            densityModel.computeCoverageHistogramDensity(x) + "\t" +
                            densityModel.computeCoverageHistogramDensityWeighted(x)
            );*/

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
