package org.example.springboot3java21demo.exercise.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class SemaphoreTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreTest.class);
    private static final int THREAD_COUNT = 30;
    private static ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    private static Semaphore semaphore = new Semaphore(2);

    public static void main(String[] args) {
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    Thread.sleep(1000*3);
                    System.out.println("save data");
                    semaphore.release();
                } catch (InterruptedException e) {
                    LOGGER.error("", e);
                }
            });
        }
        executorService.shutdown();
    }
}
