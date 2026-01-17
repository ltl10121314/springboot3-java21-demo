package org.example.springboot3java21demo.exercise.thread;

import java.util.concurrent.*;

/**
 * 线程池拋错实例
 */
public class ThreadPoolException {


    public static void main(String[] args) {
        /*
        //创建一个线程池
        ExecutorService executorService = newFixedThreadPool(1);
        //当线程池抛出异常后 submit无提示，其他线程继续执行
        executorService.submit(new Task());
        //当线程池抛出异常后 execute抛出异常，其他线程继续执行新任务
        executorService.execute(new Task());
        */
        //1.实现一个自己的线程池工厂
        ThreadFactory factory = (Runnable r) -> {
            //创建一个线程
            Thread t = new Thread(r);
            //给创建的线程设置UncaughtExceptionHandler对象 里面实现异常的默认逻辑
            Thread.setDefaultUncaughtExceptionHandler((Thread thread1, Throwable e) -> {
                System.out.println("线程工厂设置的exceptionHandler" + e.getMessage());
            });
            return t;
        };
        //2.创建一个自己定义的线程池，使用自己定义的线程工厂
        ExecutorService executorService = new ThreadPoolExecutor(
                1,
                1,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue(10),
                factory);
        // submit无提示
        executorService.submit(new Task());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("==================为检验打印结果，1秒后执行execute方法");
        // execute 方法被线程工厂factory 的UncaughtExceptionHandler捕捉到异常
        executorService.execute(new Task());
    }
}

class Task implements Runnable {
    @Override
    public void run() {
        try {
            System.out.println("进入了task方法！！！");
            int i = 1 / 0;
        } catch (Exception e) {
            System.out.println("使用了try -catch 捕获异常" + e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
