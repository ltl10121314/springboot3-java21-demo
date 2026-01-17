package org.example.springboot3java21demo.exercise.thread;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 
 */
public class TestThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestThread.class);

    public static void main(String[] args) throws Exception {

    }

    @Test
    public void testThread2() throws ExecutionException, InterruptedException {
        // 1.创建callable对象
        Callable<String> myCallableA = new ThreadTest1(1000, "Tom");
        Callable<String> myCallableB = new ThreadTest1(1000, "Jack");
        // 2.由上面的callable对象创建一个FutureTask对象
        FutureTask<String> oneTaskA = new FutureTask<String>(myCallableA);
        FutureTask<String> oneTaskB = new FutureTask<String>(myCallableB);
        // 3.由FutureTask创建一个Thread对象
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(myCallableA);
        executor.submit(myCallableB);
        // 4.开启线程
        LOGGER.info(oneTaskA.get());
        LOGGER.info(oneTaskB.get());
        Thread.sleep(10000);
        executor.shutdownNow();
    }

    @Test
    public void testThread1() throws InterruptedException, ExecutionException {
        // 1.创建callable对象
        Callable<String> myCallableA = new ThreadTest1(10000, "Tom");
        Callable<String> myCallableB = new ThreadTest1(1000, "Jack");
        // 2.由上面的callable对象创建一个FutureTask对象
        FutureTask<String> oneTaskA = new FutureTask<String>(myCallableA);
        FutureTask<String> oneTaskB = new FutureTask<String>(myCallableB);
        // 3.由FutureTask创建一个Thread对象
        Thread t1 = new Thread(oneTaskA);
        Thread t2 = new Thread(oneTaskB);

        // 4.开启线程
        t1.start();
        t2.start();
        LOGGER.info(oneTaskA.get());
        LOGGER.info(oneTaskB.get());
    }

    public static class ThreadTest1 implements Callable<String> {
        private final long sleepTime;
        private final String name;

        private ThreadTest1(long sleepTime, String name) {
            this.sleepTime = sleepTime;
            this.name = name;
        }

        @Override
        public String call() throws Exception {
            Thread.sleep(sleepTime);
            LOGGER.info(Thread.currentThread().getName() + ": " + name + ",执行callable的call方法");
            return "result: " + name;
        }
    }
}