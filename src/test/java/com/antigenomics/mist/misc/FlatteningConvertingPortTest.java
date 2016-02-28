package com.antigenomics.mist.misc;

import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.Processor;
import cc.redberry.pipe.blocks.ParallelProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FlatteningConvertingPortTest {
    private final int m = 100;

    @Test
    public void test() {
        int n = 5000;
        FlatteningConvertingPort flatteningConvertingPort = new FlatteningConvertingPort(new FactoryListImplOutputPort(n));
        SummingProcessor summingProcessor = new SummingProcessor();
        ParallelProcessor processor = new ParallelProcessor(flatteningConvertingPort,
                summingProcessor, Runtime.getRuntime().availableProcessors());

        while (processor.take() != null) ;

        Assert.assertEquals(n * m, summingProcessor.getSum());
    }

    private class SummingProcessor implements Processor<Integer, Long> {
        private final AtomicLong sum = new AtomicLong();

        public long getSum() {
            return sum.get();
        }

        @Override
        public Long process(Integer input) {
            return sum.addAndGet(input);
        }
    }

    private class FactoryImpl implements Factory<Integer> {
        private final int value;

        private FactoryImpl(int value) {
            this.value = value;
        }

        @Override
        public Integer create() {
            return value;
        }
    }

    private class FactoryListImpl implements Iterable<FactoryImpl> {
        private final List<FactoryImpl> list;

        private FactoryListImpl(int n) {
            this.list = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                list.add(new FactoryImpl(1));
            }
        }

        @Override
        public Iterator<FactoryImpl> iterator() {
            return list.iterator();
        }
    }

    private class FactoryListImplOutputPort implements OutputPort<FactoryListImpl> {
        private final Iterator<FactoryListImpl> iter;

        private FactoryListImplOutputPort(int n) {
            List<FactoryListImpl> factoryListList = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                factoryListList.add(new FactoryListImpl(m));
            }
            this.iter = factoryListList.iterator();
        }

        @Override
        public FactoryListImpl take() {
            if (!iter.hasNext())
                return null;

            return iter.next();
        }
    }
}
