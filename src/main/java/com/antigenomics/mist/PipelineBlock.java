package com.antigenomics.mist;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.util.CountingOutputPort;
import cc.redberry.pipe.util.DummyInputPort;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceReader;
import com.milaboratory.core.io.sequence.SequenceWriter;

public abstract class PipelineBlock<S extends SequenceRead> {
    public static final int PROCESSOR_BUFFER_SIZE = 2048;
    private static final int INPUT_BUFFER_SIZE = 1024;

    @SuppressWarnings("unchecked")
    private InputPort<S> output, discarded = DummyInputPort.INSTANCE;
    private OutputPort<S> input;

    protected int numberOfThreads = Runtime.getRuntime().availableProcessors();

    protected abstract OutputPort<S> prepareProcessor();

    protected abstract void reportProgress();

    public void run() throws InterruptedException {
        if (input instanceof Merger) {
            ((Merger) input).start();
        }

        OutputPort<S> processorResults = prepareProcessor();

        Thread reporter = new Thread(() -> {
            try {
                while (true) {
                    reportProgress();
                    Thread.sleep(10000);
                }
            } catch (InterruptedException e) {
                reportProgress();
            }
        });

        reporter.setDaemon(true);
        reporter.start();

        CUtils.drain(processorResults, output);

        reporter.interrupt();
    }

    public void setOutput(SequenceWriter<S> writer) {
        this.output = new SequenceWriterWrapper<>(writer);
    }

    public void setOutput(InputPort<S> output) {
        this.output = output;
    }

    public void setDiscarded(InputPort<S> discarded) {
        this.discarded = discarded;
    }

    public void setDiscarded(SequenceWriter<S> writer) {
        this.discarded = new SequenceWriterWrapper<>(writer);
    }

    public void setInput(OutputPort<S> input) {
        if (input instanceof SequenceReader) {
            Merger<S> bufferedInput = new Merger<>(INPUT_BUFFER_SIZE);
            bufferedInput.merge(input);
            this.input = bufferedInput;
        } else {
            this.input = input;
        }
    }



    public InputPort<S> getOutput() {
        return output;
    }

    public InputPort<S> getDiscarded() {
        return discarded;
    }

    public OutputPort<S> getInput() {
        return input;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }
}
