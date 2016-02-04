package com.antigenomics.mist.umi;

import com.antigenomics.mist.TestUtil;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class UmiErrorAndDiversityModelTest {
    @Test
    public void diversityTest() {
        int size = 10, numberOfUmis = 10000;

        UmiErrorAndDiversityModel umiErrorAndDiversityModel = new UmiErrorAndDiversityModel();

        byte[] quality = new byte[size];
        Arrays.fill(quality, SequenceQuality.GOOD_QUALITY_VALUE);

        for (int i = 0; i < numberOfUmis; i++) {
            NucleotideSequence umi = TestUtil.randomSequence(size);

            umiErrorAndDiversityModel.update(new UmiCoverageAndQuality(new UmiTag("test", umi),
                    1, new SequenceQuality(quality)));
        }

        double expectedDiversity = Math.pow(4, size);

        Assert.assertTrue(Math.abs(expectedDiversity - umiErrorAndDiversityModel.computeDiversity())
                / expectedDiversity < 0.01);
    }
}
