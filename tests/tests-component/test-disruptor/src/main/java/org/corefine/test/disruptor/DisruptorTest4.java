package org.corefine.test.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fe by 2021/11/11 11:39
 */
public class DisruptorTest4 {
    public static class LongEventFactory implements EventFactory<LongEvent> {
        public LongEvent newInstance() {
            return new LongEvent();
        }
    }

    public static class Eout implements WorkHandler<LongEvent> {
        public void onEvent(LongEvent event) throws Exception {
            if (event.list != null) {
                System.out.println(getClass().getSimpleName() + " " + Thread.currentThread() + ": " + event);
                Thread.sleep(2000);
            }
        }
    }
    public static class Eout2 implements WorkHandler<LongEvent> {
        public void onEvent(LongEvent event) throws Exception {
            if (event.list != null) {
                System.out.println(getClass().getSimpleName() + " " + Thread.currentThread() + ": " + event);
                Thread.sleep(3000);
            }
        }
    }

    public static class LongEvent {
        private long i0;
        private List<Long> list;

        public String toString() {
            return " " + list;
        }
    }

    public static class E1 implements EventHandler<LongEvent> {
        private volatile List<Long> list = new ArrayList<>();
        private long preTime;
        @Override
        public void onEvent(LongEvent event, long sequence, boolean endOfBatch) throws Exception {
            list.add(event.i0);
            if (endOfBatch) {
                if (list.size() < 5) {
                    long current = System.currentTimeMillis();
                    if (preTime + 500 > current) {
                        System.out.println("sleep: " + 5000);
                        Thread.sleep(5000);
                        preTime = current;
                        return;
                    }
                    preTime = current;
                }
                List<Long> list = this.list;
                this.list = new ArrayList<>();
                System.out.println("end:" + list.size());
                event.list = list;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Disruptor<LongEvent> disruptor = new Disruptor<>(new LongEventFactory(), 64,  DaemonThreadFactory.INSTANCE);
        Eout e1 = new Eout();
        Eout2 e2 = new Eout2();
        disruptor.handleEventsWith(new E1())
//                .thenHandleEventsWithWorkerPool(e1, e1, e1, e1, e1, e1, e1, e1)
                .thenHandleEventsWithWorkerPool(e2, e2, e2, e2, e2, e2, e2, e2, e2, e2, e2, e2);
        disruptor.start();

        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        for (long l = 0; l < 1000; l++) {
            ringBuffer.publishEvent(new EventTranslatorOneArg<LongEvent, Long>() {
                public void translateTo(LongEvent event, long sequence, Long arg0) {
                    event.i0 = arg0;
//                    System.out.println(Thread.currentThread().getName() + ": " + ringBuffer.remainingCapacity() + ",  " + ringBuffer.getMinimumGatingSequence());
                }
            }, l);
        }
        Thread.sleep(100000);
    }
}
