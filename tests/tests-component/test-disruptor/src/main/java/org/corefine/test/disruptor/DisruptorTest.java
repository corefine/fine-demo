package org.corefine.test.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;

/**
 * @author Fe by 2021/11/11 11:39
 */
public class DisruptorTest {
    public static class LongEvent {
        private String value;

        public void set(String value) {
            this.value = value;
        }
    }

//    public static class LongEventFactory implements EventFactory<LongEvent> {
//        public LongEvent newInstance() {
//            return new LongEvent();
//        }
//    }
//
//    public static class LongEventHandler implements EventHandler<LongEvent> {
//        public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
//            System.out.println("Event: " + event);
//        }
//    }

    public static void main(String[] args) throws InterruptedException {
        int bufferSize = 64;
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);
        disruptor.handleEventsWith(
                        (event, sequence, endOfBatch) -> {
                            event.set(event.value + "@");
                            System.out.println(Thread.currentThread().getName() + ", Event1: " + event.value);
                            Thread.sleep(201);
                        },
                        (event, sequence, endOfBatch) -> {
                            event.set(event.value + "#");
                            System.out.println(Thread.currentThread().getName() + ", Event2: " + event.value);
                            Thread.sleep(200);
                        });
        disruptor.start();


        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++) {
            bb.putLong(0, l);
            ringBuffer.publishEvent((event, sequence, buffer) -> event.set(Long.toString(buffer.getLong(0))), bb);
            System.out.println(Thread.currentThread().getName() + ", pushed: " + l);
        }
    }
}
