package com.antigenomics.mist.assemble;

import com.antigenomics.mist.ReadGenerator;
import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AssemblerTest {
    @Test
    public void emptyTest() {
        SingleAssembler assembler = new SingleAssembler();
        AssemblyResult<SingleRead> result = assembler.process(new Mig<>(null, new ArrayList<>()));

        Assert.assertTrue(result.getConsensuses().isEmpty());
    }

    @Test
    public void singleTest() {
        SingleAssembler assembler = new SingleAssembler();
        ReadGenerator readGenerator = new ReadGenerator();

        List<SingleRead> reads = new ArrayList<>();

        reads.add(new SingleReadImpl(-1,
                readGenerator.randomRead(100, (byte) 30),
                ""));
        AssemblyResult<SingleRead> result = assembler.process(new Mig<>(null, reads));

        Assert.assertEquals(reads.get(0).getData().getSequence(),
                result.getConsensuses().get(0).asRead().getData().getSequence());
        Assert.assertEquals(1, result.getConsensuses().size());
        Assert.assertTrue(result.getDiscardedReads().isEmpty());
    }
}
