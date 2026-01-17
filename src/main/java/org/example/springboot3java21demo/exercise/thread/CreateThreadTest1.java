package org.example.springboot3java21demo.exercise.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class CreateThreadTest1 extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateThreadTest1.class);
    int i = 0;

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            LOGGER.error("{}  : {}", Thread.currentThread().getName(), i);
            if (i == 20) {
                new CreateThreadTest1().start();
                new CreateThreadTest1().start();
            }
        }
    }

    @Override
    public void run() {
        for (; i < 100; i++) {
            LOGGER.error("{} {}", Thread.currentThread().getName(), i);
        }
    }
}
