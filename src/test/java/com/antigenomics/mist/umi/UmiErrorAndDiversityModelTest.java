package com.antigenomics.mist.umi;

import com.antigenomics.mist.ReadGenerator;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class UmiErrorAndDiversityModelTest {
    @Test
    public void diversityTest() {
        ReadGenerator readGenerator = new ReadGenerator();

        int size = 10, numberOfUmis = 10000;

        UmiErrorAndDiversityModel umiErrorAndDiversityModel = new UmiErrorAndDiversityModel();

        byte[] quality = new byte[size];
        Arrays.fill(quality, SequenceQuality.GOOD_QUALITY_VALUE);

        for (int i = 0; i < numberOfUmis; i++) {
            NucleotideSequence umi = readGenerator.randomSequence(size);

            umiErrorAndDiversityModel.put(new UmiCoverageAndQuality(new UmiTag(umi),
                    1, new SequenceQuality(quality)));
        }

        double expectedDiversity = Math.pow(4, size);

        Assert.assertTrue(Math.abs(expectedDiversity - umiErrorAndDiversityModel.computeExpectedDiversity())
                / expectedDiversity < 0.01);
    }

    @Test
    public void errorPvalueTest() {
        SyntheticUmiReadout syntheticUmiReadout = new SyntheticUmiReadout(10000, 12, (byte) 35, 4.0, 1.0);

        UmiErrorAndDiversityModel umiErrorAndDiversityModel = syntheticUmiReadout.getUmiErrorAndDiversityModel();

        SummaryStatistics errorStatSummary = new SummaryStatistics();

        syntheticUmiReadout.getReads()
                .stream()
                .filter(SyntheticUmiReadout.UmiParentChildPair::isError)
                .map(x -> umiErrorAndDiversityModel.errorPValue(x.getParentCoverageAndQuality(),
                        x.getChildCoverageAndQuality()))
                .forEach(errorStatSummary::addValue);

        System.out.println("Error probs (synthetic, errors only):\n" + errorStatSummary);

        Assert.assertTrue(errorStatSummary.getGeometricMean() < 0.05);
    }

    @Test
    public void assemblyProbTest() {
        SyntheticUmiReadout syntheticUmiReadout = new SyntheticUmiReadout(10000, 12, (byte) 35, 4.0, 1.0);

        UmiErrorAndDiversityModel umiErrorAndDiversityModel = syntheticUmiReadout.getUmiErrorAndDiversityModel();

        SummaryStatistics assemblyProbSummary = new SummaryStatistics();

        syntheticUmiReadout.getReads()
                .stream()
                .filter(SyntheticUmiReadout.UmiParentChildPair::isError)
                .map(x -> umiErrorAndDiversityModel.independentAssemblyProbability(x.getParent(),
                        x.getChild().getSequence()))
                .forEach(assemblyProbSummary::addValue);

        System.out.println("Assembly probability (synthetic, errors only):\n" + assemblyProbSummary);

        Assert.assertTrue((assemblyProbSummary.getMean() * syntheticUmiReadout.getNumberOfUmis()) < 0.05);
    }

    @Test
    public void shortUmiAssemblyProbTest() {
        ReadGenerator readGenerator = new ReadGenerator();

        SyntheticUmiReadout syntheticUmiReadout = new SyntheticUmiReadout(1000, 4, (byte) 40, 0.0, 1e-16);

        UmiErrorAndDiversityModel umiErrorAndDiversityModel = syntheticUmiReadout.getUmiErrorAndDiversityModel();

        SummaryStatistics assemblyProbSummary = new SummaryStatistics();

        syntheticUmiReadout.getUmis().stream().map(x ->
                umiErrorAndDiversityModel.independentAssemblyProbability(x,
                        readGenerator.randomSequence(4))).forEach(assemblyProbSummary::addValue);

        System.out.println("Assembly probability (synthetic, short UMI):\n" + assemblyProbSummary);
        Assert.assertTrue(Math.abs(assemblyProbSummary.getMean() - 0.5) <=
                assemblyProbSummary.getStandardDeviation());
    }
}
