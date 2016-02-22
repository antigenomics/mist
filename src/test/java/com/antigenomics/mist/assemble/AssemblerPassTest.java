package com.antigenomics.mist.assemble;

import com.antigenomics.mist.ReadGenerator;
import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AssemblerPassTest {
    @Test
    public void simpleSyntheticMigTest() {
        MigGenerator migGenerator = new MigGenerator(12,
                100, 0, 0, (byte) 25, 6.0);

        SingleAssembler assembler = new SingleAssembler();

        int total = 1000, correctAssembly = 0;

        for (int i = 0; i < total; i++) {
            MigGenerator.SyntheticMig mig = migGenerator.take();

            if (mig.getConsensus().getSequence().equals(
                    assembler.assemblePass(mig.getReads()).getConsensus().getSequence())) {
                correctAssembly++;
            }
        }

        Assert.assertTrue(correctAssembly / (float) total > 0.95);
    }

    @Test
    public void randomReadGroupsTest() {
        SingleAssembler assembler = new SingleAssembler();
        ReadGenerator readGenerator = new ReadGenerator();

        int total = 1000, falsePositive = 0;

        for (int i = 0; i < total; i++) {
            List<SingleRead> reads = new ArrayList<>();

            for (int j = 0; j < 5; j++) {
                reads.add(new SingleReadImpl(-1,
                        readGenerator.randomRead(100, (byte) 30),
                        ""));
            }

            Assembler<SingleRead>.AssemblyPassResult passResult = assembler.assemblePass(reads);

            if (passResult.isGood()) {
                falsePositive++;
            } else {
                Assert.assertTrue(passResult.getDiscardedReads().size() == reads.size());
            }
        }

        System.out.println(falsePositive);

        Assert.assertTrue(falsePositive / (float) total == 0);
    }

    @Test
    public void emptyTest() {
        SingleAssembler assembler = new SingleAssembler();
        Assembler<SingleRead>.AssemblyPassResult passResult = assembler.assemblePass(new ArrayList<>());

        Assert.assertTrue(passResult == null);
    }

    @Test
    public void singleTest() {
        SingleAssembler assembler = new SingleAssembler();
        ReadGenerator readGenerator = new ReadGenerator();

        List<SingleRead> reads = new ArrayList<>();

        reads.add(new SingleReadImpl(-1,
                readGenerator.randomRead(100, (byte) 30),
                ""));

        Assembler<SingleRead>.AssemblyPassResult passResult = assembler.assemblePass(reads);

        Assert.assertEquals(reads.get(0).getData().getSequence(),
                passResult.getConsensus().getSequence());
    }
}
