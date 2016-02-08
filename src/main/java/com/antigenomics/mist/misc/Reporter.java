package com.antigenomics.mist.misc;

import cc.redberry.pipe.util.CountingOutputPort;

public abstract class Reporter implements Runnable {
    private final CountingOutputPort countingInputPort;
    private long prevCount = -1;

    public Reporter(CountingOutputPort countingInputPort) {
        this.countingInputPort = countingInputPort;
    }

    @Override
    public void run() {
        try {
            while (!countingInputPort.isClosed()) {
                long count = countingInputPort.getCount();
                if (prevCount != count) {
                    report();
                    prevCount = count;
                }
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            report();
        }
    }

    protected abstract void report();
}
