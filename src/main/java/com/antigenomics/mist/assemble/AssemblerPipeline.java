package com.antigenomics.mist.assemble;

import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.Buffer;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountingOutputPort;
import com.antigenomics.mist.PipelineBlock;
import com.antigenomics.mist.misc.Reporter;
import com.antigenomics.mist.misc.Speaker;
import com.milaboratory.core.io.sequence.SequenceRead;

public class AssemblerPipeline<S extends SequenceRead> extends PipelineBlock<Mig<S>, S> {
    private final Assembler<S> assembler;

    public AssemblerPipeline(Assembler<S> assembler) {
        this.assembler = assembler;
        // TODO: runtime parameters
    }

    @Override
    protected OutputPort<S> prepareProcessor(OutputPort<Mig<S>> input, InputPort<Mig<S>> discarded) {
        final OutputPort<AssemblyResult<S>> assemblyResults = new ParallelProcessor<>(input,
                assembler, numberOfThreads);

        // TODO: discarded reads

        // Flatten -- todo - check
        Buffer<S> buffer = new Buffer<>();

        final InputPort<S> bufferInput = buffer.createInputPort();

        Thread transferThread = new Thread(() -> {
            AssemblyResult<S> result;

            while ((result = assemblyResults.take()) != null) {
                for (Consensus<S> consensus : result.getConsensuses()) {
                    bufferInput.put(consensus.asRead());
                }
            }
        });

        transferThread.run();

        return buffer;
    }

    @Override
    protected void reportProgress() {
        // TODO:
    }
}
