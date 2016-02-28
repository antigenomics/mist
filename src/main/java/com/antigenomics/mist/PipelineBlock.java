package com.antigenomics.mist;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.util.DummyInputPort;

public abstract class PipelineBlock<I, O> {
    private static final int INPUT_BUFFER_SIZE = 1024;

    public PipelineBlock() {
    }

    public PipelineBlock(OutputPort<I> input, InputPort<O> output) {
        this.input = input;
        this.output = output;
    }

    public PipelineBlock(OutputPort<I> input, InputPort<O> output, InputPort<I> discarded) {
        this.input = input;
        this.output = output;
        this.discarded = discarded;
    }

    private OutputPort<I> input;
    @SuppressWarnings("unchecked")
    private InputPort<O> output = DummyInputPort.INSTANCE;
    @SuppressWarnings("unchecked")
    private InputPort<I> discarded = DummyInputPort.INSTANCE;

    protected int numberOfThreads = Runtime.getRuntime().availableProcessors();

    protected abstract OutputPort<O> prepareProcessor(OutputPort<I> input, InputPort<I> discarded);

    protected abstract void reportProgress();

    @SuppressWarnings("unchecked")
    public void run() throws InterruptedException {
        if (input == null) {
            throw new RuntimeException("Input not set.");
        }

        // Ensure thread-safety & performance
        final Merger<I> bufferedInput;

        if (input instanceof Merger) {
            bufferedInput = (Merger) input;
        } else {
            bufferedInput = new Merger<>(INPUT_BUFFER_SIZE);
            bufferedInput.merge(input);
        }

        bufferedInput.start();

        final OutputPort<O> processorResults = prepareProcessor(bufferedInput, discarded);

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

    public void setInput(OutputPort<I> input) {
        this.input = input;
    }

    public void setOutput(InputPort<O> output) {
        this.output = output;
    }

    public void setDiscarded(InputPort<I> discarded) {
        this.discarded = discarded;
    }

    public OutputPort<I> getInput() {
        return input;
    }

    public InputPort<O> getOutput() {
        return output;
    }

    public InputPort<I> getDiscarded() {
        return discarded;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }
}
