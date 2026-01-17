package org.example.springboot3java21demo.exercise.thread.abc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class AbcSynchronized {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbcSynchronized.class);

    public static void main(String[] args) throws InterruptedException {
        Object tA = new Object();
        Object tB = new Object();
        Object tC = new Object();
        ThreadPrinter a = new ThreadPrinter("A", tC, tA);
        ThreadPrinter b = new ThreadPrinter("B", tA, tB);
        ThreadPrinter c = new ThreadPrinter("C", tB, tC);
        new Thread(a).start();
        Thread.sleep(10);
        new Thread(b).start();
        Thread.sleep(10);
        new Thread(c).start();
    }

    public static class ThreadPrinter implements Runnable {
        private final String name;
        private final Object prev;
        private final Object self;

        private ThreadPrinter(String name, Object prev, Object self) {
            this.name = name;
            this.prev = prev;
            this.self = self;
        }

        @Override
        public void run() {
            int total = 10;
            int count = 0;
            while (count < total) {
                synchronized (prev) {
                    synchronized (self) {
                        System.out.print(name);
                        count++;
                        self.notifyAll();
                    }
                    try {
                        if (total == count) {
                            prev.notifyAll();
                        } else {
                            prev.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

}
