package org.corefine.test.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Fe by 2021/11/11 11:39
 */
public class DisruptorTest2 {
    public static class LongEvent {
        private String value;
        public void set(String value) {
            this.value = value;
        }
    }

    public static class LongEventFactory implements EventFactory<LongEvent> {
        public LongEvent newInstance() {
            return new LongEvent();
        }
    }

    public static class Consumer implements WorkHandler<LongEvent> {

        @Override
        public void onEvent(LongEvent event) throws Exception {
            System.out.println(Thread.currentThread().getName() + ", Event1: " + event.value);
            Thread.sleep(2000);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadFactory producerFactory = Executors.defaultThreadFactory();
//        Disruptor<LongEvent> disruptor = new Disruptor<>(new LongEventFactory(), 1024,  DaemonThreadFactory.INSTANCE);
        Disruptor<LongEvent> disruptor = new Disruptor<>(new LongEventFactory(), 64,  producerFactory,
                ProducerType.SINGLE, new BlockingWaitStrategy());
        Consumer[] consumers = new Consumer[300];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new Consumer();
        }
        disruptor.handleEventsWithWorkerPool(consumers);
        disruptor.start();


        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        for (long l = 0; true; l++) {
            final long l0 = l;
            EventTranslator<LongEvent> translator = new EventTranslator<LongEvent>() {
                @Override
                public void translateTo(LongEvent event, long sequence) {
                    event.set(Long.toString(l0));
                }
            };
            ringBuffer.publishEvent(translator);
            System.out.println(Thread.currentThread().getName() + ", pushed: " + l);
        }
    }
}
