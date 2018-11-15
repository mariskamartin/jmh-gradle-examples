package cz.mariskamartin.producerConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ConsumerWorkerJmh<T> implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ConsumerWorkerJmh.class);
    private static AtomicLong consumerIdGenerator = new AtomicLong();
    private final long consumerId;
    private final T done;
    private BlockingQueue<T> inputQueue;

    public ConsumerWorkerJmh(BlockingQueue<T> inputQueue, T done) {
        this.consumerId = consumerIdGenerator.incrementAndGet();
        this.inputQueue = inputQueue;
        this.done = done;
    }

    @Override
    public void run() {
        log.info("Consumer " + consumerId + " (" + Thread.currentThread().getName() + "): STARTED");
        while (true) {
            try {
                T item = inputQueue.take(); //here are locks
//                Thread.yield();
                if (item == this.done) {
                    log.info("Consumer " + consumerId + " (" + Thread.currentThread().getName() + "): receive DONE");
                    inputQueue.add(this.done);
                    break;
                }
                handle(item);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                break;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("Consumer " + consumerId + " (" + Thread.currentThread().getName() + "): STOPS");
    }

    public void handle(T item) {
        //pls override
    }
}
