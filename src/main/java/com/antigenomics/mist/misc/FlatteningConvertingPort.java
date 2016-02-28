package com.antigenomics.mist.misc;

import cc.redberry.pipe.OutputPort;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FlatteningConvertingPort<T1 extends Iterable<Factory<T2>>, T2> implements OutputPort<T2> {
    private final ConcurrentLinkedQueue<T2> buffer;

    public FlatteningConvertingPort(OutputPort<T1> input) {
        this.buffer = new ConcurrentLinkedQueue<>();

        Thread transferThread = new Thread(() -> {
            try {
                T1 object;
                while ((object = input.take()) != null) {
                    for (Factory<T2> factory : object) {
                        buffer.add(factory.create());
                    }
                }
            } catch (RuntimeException re) {
                throw new RuntimeException(re);
            }
        });

        transferThread.run();
    }

    @Override
    public T2 take() {
        return buffer.poll();
    }
}
