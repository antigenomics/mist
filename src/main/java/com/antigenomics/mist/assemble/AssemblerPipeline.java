package com.antigenomics.mist.assemble;

import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountingOutputPort;
import com.antigenomics.mist.PipelineBlock;
import com.antigenomics.mist.assemble.ioadapter.MigProvider;
import com.antigenomics.mist.misc.Reporter;
import com.antigenomics.mist.misc.Speaker;
import com.milaboratory.core.io.sequence.SequenceRead;

public class AssemblerPipeline<S extends SequenceRead> extends PipelineBlock<S> {
    private final Assembler<S> assembler;
    private final MigProvider<S> migProvider;
    private final InputPort<S> output;

    public AssemblerPipeline(Assembler<S> assembler, MigProvider<S> migProvider, InputPort<S> output) {
        this.assembler = assembler;
        this.migProvider = migProvider;
        this.output = output;
        // TODO: runtime parameters
    }

    @Override
    public void run() {
        final CountingOutputPort<Mig<S>> countingInput = new CountingOutputPort<>(migProvider);

        Thread reporter = new Thread(new Reporter(countingInput) {
            @Override
            protected void report() {
                Speaker.INSTANCE.sout("Loaded " + countingInput.getCount() + " MIGs.");
                // TODO
            }
        });

        reporter.setDaemon(true);
        reporter.start();

        final OutputPort<AssemblyResult<S>> searchResults = new ParallelProcessor<>(countingInput,
                assembler, Runtime.getRuntime().availableProcessors());

        AssemblyResult<S> result;

        while ((result = searchResults.take()) != null) {
            for (Consensus<S> consensus : result.getConsensuses()) {
                output.put(consensus.asRead());
            }
        }

        output.put(null);

        reporter.interrupt();
    }
}
