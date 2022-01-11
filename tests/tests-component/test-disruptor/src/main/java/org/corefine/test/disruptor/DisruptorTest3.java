package org.corefine.test.disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.util.DaemonThreadFactory;

/**
 * @author Fe by 2021/11/11 11:39
 */
public class DisruptorTest3 {
    public static class LongEvent {
        private int i0;
        private int i1;
        private int i2;
        private int i3;

        public String toString() {
            return "" + i0 + " " + i1 + " " + i2 + " " + i3;
        }
    }

    public static class LongEventFactory implements EventFactory<LongEvent> {
        public LongEvent newInstance() {
            return new LongEvent();
        }
    }

    public static class E1 implements EventHandler<LongEvent>, WorkHandler<LongEvent> {
        @Override
        public void onEvent(LongEvent event, long sequence, boolean endOfBatch) throws Exception {
            onEvent(event);
        }

        @Override
        public void onEvent(LongEvent event) throws Exception {
            Thread.sleep(2000);
            event.i1 = (int) System.currentTimeMillis() % 9 + 10;
            System.out.println(getClass().getSimpleName() + " " + Thread.currentThread() + ": " + event);
        }
    }

    public static class E2 implements EventHandler<LongEvent>, WorkHandler<LongEvent> {
        @Override
        public void onEvent(LongEvent event, long sequence, boolean endOfBatch) throws Exception {
            onEvent(event);
        }

        @Override
        public void onEvent(LongEvent event) throws Exception {
            Thread.sleep(3000);
            event.i2 = (int) System.currentTimeMillis() % 8 + 20;
            System.out.println(getClass().getSimpleName() + " " + Thread.currentThread() + ": " + event);

        }
    }

    public static class E3 implements EventHandler<LongEvent>, WorkHandler<LongEvent> {
        @Override
        public void onEvent(LongEvent event, long sequence, boolean endOfBatch) throws Exception {
            onEvent(event);
        }

        @Override
        public void onEvent(LongEvent event) throws Exception {
            event.i3 = (int) System.currentTimeMillis() % 7 + 30;
            System.out.println(getClass().getSimpleName() + " " + Thread.currentThread() + ": " + event);

        }
    }

    public static class Eout implements EventHandler<LongEvent>, WorkHandler<LongEvent> {
        @Override
        public void onEvent(LongEvent event, long sequence, boolean endOfBatch) throws Exception {
            onEvent(event);
        }

        @Override
        public void onEvent(LongEvent event) throws Exception {
            System.err.println(getClass().getSimpleName() + " " + Thread.currentThread() + ": " + event);
        }
    }
    public static void main(String[] args) {
        Disruptor<LongEvent> disruptor = new Disruptor<>(new LongEventFactory(), 16,  DaemonThreadFactory.INSTANCE);
        E1 e1 = new E1();
        E2 e2 = new E2();
        E3 e3 = new E3();
        E1[] e1s = {e1, e1, e1};
        E2[] e2s = {e2, e2, e2};
        E3[] e3s = {e3, e3, e3};
        EventHandlerGroup<LongEvent> handlerGroup = disruptor.handleEventsWithWorkerPool(e1s);
        handlerGroup = handlerGroup.thenHandleEventsWithWorkerPool(e2s);
        handlerGroup = handlerGroup.thenHandleEventsWithWorkerPool(e3s);
        handlerGroup.then(new Eout());
        disruptor.start();

        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        for (int l = 1; true; l++) {
            long index = ringBuffer.next();
            LongEvent event = ringBuffer.get(index);
            event.i0 = l;
            System.out.println(ringBuffer.remainingCapacity() + ",  " + ringBuffer.getMinimumGatingSequence());
            ringBuffer.publish(index);
        }
    }
}
