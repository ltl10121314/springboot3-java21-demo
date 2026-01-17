package org.example.springboot3java21demo.exercise.thread.abc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 
 */
public class PrintAbcTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintAbcTest.class);

    @Test
    public void printAbcTest1() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Thread threadA = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                LOGGER.info("A");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            countDownLatch.countDown();
        }, "Thread-A");
        threadA.start();

        Thread threadB = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                LOGGER.info("B");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            countDownLatch.countDown();
        }, "Thread-B");
        threadB.start();
        countDownLatch.await();
    }

}
