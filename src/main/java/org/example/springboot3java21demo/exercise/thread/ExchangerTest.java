package org.example.springboot3java21demo.exercise.thread;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExchangerTest {

    private static final Exchanger<String> exchanger = new Exchanger<>();
    private static ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        executorService.execute(() -> {
                String A = "银行流水A";
            try {
                Thread.sleep(1000*3);
                exchanger.exchange(A);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.execute(() -> {
            String B = "银行流水B";
            try {
                String A = exchanger.exchange(B);
                System.out.println("A: " + A);
                System.out.println("B: " + B);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.shutdown();
    }
}
