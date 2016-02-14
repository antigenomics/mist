package com.antigenomics.mist;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.util.DummyInputPort;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceWriter;

public abstract class PipelineBlock<S extends SequenceRead> {
    @SuppressWarnings("unchecked")
    private InputPort<S> output, discarded = DummyInputPort.INSTANCE;
    private OutputPort<S> input;

    protected abstract OutputPort<S> prepareProcessor();

    protected abstract void reportProgress();

    public void run() throws InterruptedException {
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
        this.input = input;
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
}
