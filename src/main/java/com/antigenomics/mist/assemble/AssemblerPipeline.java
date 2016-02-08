package com.antigenomics.mist.assemble;

import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountingOutputPort;
import com.antigenomics.mist.assemble.ioadapter.MigProvider;
import com.antigenomics.mist.misc.Reporter;
import com.antigenomics.mist.misc.Speaker;
import com.milaboratory.core.io.sequence.SequenceRead;

public class AssemblerPipeline<T extends SequenceRead> implements Runnable{
    private final Assembler<T> assembler;
    private final MigProvider<T> migProvider;
    private final InputPort<T> output;

    public AssemblerPipeline(Assembler<T> assembler, MigProvider<T> migProvider, InputPort<T> output) {
        this.assembler = assembler;
        this.migProvider = migProvider;
        this.output = output;
        // TODO: runtime parameters
    }

    @Override
    public void run() {
        final CountingOutputPort<Mig<T>> countingInput = new CountingOutputPort<>(migProvider);

        Thread reporter = new Thread(new Reporter(countingInput) {
            @Override
            protected void report() {
                Speaker.INSTANCE.sout("Loaded " + countingInput.getCount() + " MIGs.");
                // TODO
            }
        });

        reporter.setDaemon(true);
        reporter.start();

        final OutputPort<AssemblyResult<T>> searchResults = new ParallelProcessor<>(countingInput,
                assembler, Runtime.getRuntime().availableProcessors());

        AssemblyResult<T> result;

        while ((result = searchResults.take()) != null) {
            for (Consensus<T> consensus : result.getConsensuses()) {
                output.put(consensus.asRead());
            }
        }

        output.put(null);

        reporter.interrupt();
    }
}
