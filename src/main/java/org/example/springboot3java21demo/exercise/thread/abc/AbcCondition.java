package org.example.springboot3java21demo.exercise.thread.abc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 */
public class AbcCondition {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbcCondition.class);
    private static final Lock lock = new ReentrantLock();
    private static final Condition A = lock.newCondition();
    private static final Condition B = lock.newCondition();
    private static final Condition C = lock.newCondition();
    private static int count = 0;

    public static void main(String[] args) {
        new ThreadA().start();
        new ThreadB().start();
        new ThreadC().start();
    }

    static class ThreadA extends Thread {
        @Override
        public void run() {
            lock.lock();
            try {
                for (int i = 0; i < 10; i++) {
                    while (count % 3 != 0) {//注意这里是不等于0，也就是说在count % 3为0之前，当前线程一直阻塞状态
                        A.await(); // A释放lock锁
                    }
                    LOGGER.error("A");
                    count++;
                    B.signal(); // A执行完唤醒B线程
                }
            } catch (InterruptedException e) {
                LOGGER.error("ThreadA error:", e);
            } finally {
                lock.unlock();
            }
        }
    }

    static class ThreadB extends Thread {
        @Override
        public void run() {
            lock.lock();
            try {
                for (int i = 0; i < 10; i++) {
                    while (count % 3 != 1) {//注意这里是不等于0，也就是说在count % 3为0之前，当前线程一直阻塞状态
                        B.await(); // A释放lock锁
                    }
                    LOGGER.error("B");
                    count++;
                    C.signal(); // A执行完唤醒B线程
                }
            } catch (InterruptedException e) {
                LOGGER.error("ThreadB error:", e);
            } finally {
                lock.unlock();
            }
        }
    }

    static class ThreadC extends Thread {
        @Override
        public void run() {
            lock.lock();
            try {
                for (int i = 0; i < 10; i++) {
                    while (count % 3 != 2) {
                        C.await();
                    }
                    System.out.print("C");
                    count++;
                    A.signal();
                }
            } catch (InterruptedException e) {
                LOGGER.error("ThreadC error:", e);
            } finally {
                lock.unlock();
            }
        }
    }
}
