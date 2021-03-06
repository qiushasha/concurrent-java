package problem5.producerconsumer;

import problem5.producerconsumer.blockingqueue.BlockingQueueConsumer;
import problem5.producerconsumer.blockingqueue.BlockingQueueProducer;
import problem5.producerconsumer.condition.ConditionConsumer;
import problem5.producerconsumer.condition.ConditionProducer;
import problem5.producerconsumer.semaphore.SemaphoreConsumer;
import problem5.producerconsumer.semaphore.SemaphoreProducer;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kennylbj on 16/9/10.
 * implement 3 versions of P/C
 * 1) use Semaphore
 * 2) use Condition
 * 3) use BlockingQueue
 * All versions support multiple producers and consumers
 */
public class Main {
    private final static int BUFFER_SIZE = 100;
    private final static int PRODUCER_NUM = 3;
    private final static int CONSUMER_NUM = 2;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newCachedThreadPool();

        //buffers of all P/C
        Buffer<Item> semaphoreBuffer = new LinkListBuffer(BUFFER_SIZE);
        Buffer<Item> conditionBuffer = new LinkListBuffer(BUFFER_SIZE);
        BlockingQueue<Item> blockingQueueBuffer = new LinkedBlockingQueue<>(BUFFER_SIZE);

        //semaphores for Semaphore version of P/C
        Semaphore fullCount = new Semaphore(0);
        Semaphore emptyCount = new Semaphore(BUFFER_SIZE);

        //lock and conditions for Condition version of P/C
        Lock lock = new ReentrantLock();
        Condition full = lock.newCondition();
        Condition empty = lock.newCondition();

        for (int i = 0; i < PRODUCER_NUM; i++) {
            pool.execute(new SemaphoreProducer(semaphoreBuffer, fullCount, emptyCount));
            pool.execute(new ConditionProducer(conditionBuffer, lock, full, empty));
            pool.execute(new BlockingQueueProducer(blockingQueueBuffer));
        }
        for (int i = 0; i < CONSUMER_NUM; i++) {
            pool.execute(new SemaphoreConsumer(semaphoreBuffer, fullCount, emptyCount));
            pool.execute(new ConditionConsumer(conditionBuffer, lock, full, empty));
            pool.execute(new BlockingQueueConsumer(blockingQueueBuffer));
        }

        Thread.sleep(10 * 1000);

        pool.shutdownNow();
        pool.awaitTermination(3, TimeUnit.SECONDS);

        System.out.println("buffer size " + semaphoreBuffer.getSize()
                + " : " + conditionBuffer.getSize() + " : " + blockingQueueBuffer.size());
    }
}
