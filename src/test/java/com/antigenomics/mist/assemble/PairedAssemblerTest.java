package com.antigenomics.mist.assemble;

import com.antigenomics.mist.ReadGenerator;
import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PairedAssemblerTest {
    @Test
    public void emptyTest() {
        PairedAssembler assembler = new PairedAssembler();
        AssemblyResult<PairedRead> result = assembler.process(new Mig<>(null, new ArrayList<>()));

        Assert.assertTrue(result.getConsensuses().isEmpty());
    }

    @Test
    public void oneReadTest() {
        PairedAssembler assembler = new PairedAssembler();
        ReadGenerator readGenerator = new ReadGenerator();

        List<PairedRead> reads = new ArrayList<>();

        reads.add(new PairedRead(new SingleReadImpl(-1,
                readGenerator.randomRead(100, (byte) 30),
                ""), new SingleReadImpl(-1,
                readGenerator.randomRead(100, (byte) 30),
                "")));

        AssemblyResult<PairedRead> result = assembler.process(new Mig<>(null, reads));

        Assert.assertEquals(reads.get(0).getR1().getData().getSequence(),
                ((PairedConsensus) result.getConsensuses().get(0)).getConsensus1().getConsensusNSQ().getSequence());
        Assert.assertEquals(reads.get(0).getR2().getData().getSequence(),
                ((PairedConsensus) result.getConsensuses().get(0)).getConsensus2().getConsensusNSQ().getSequence());
        Assert.assertEquals(1, result.getConsensuses().size());
        Assert.assertTrue(result.getDiscardedReads().isEmpty());
    }
}
