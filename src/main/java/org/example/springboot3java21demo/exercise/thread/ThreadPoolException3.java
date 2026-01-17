package org.example.springboot3java21demo.exercise.thread;

import java.util.concurrent.*;

/**
 * 线程池拋错
 */
public class ThreadPoolException3 {


    public static void main(String[] args) throws InterruptedException, ExecutionException {

        //1.创建一个自己定义的线程池
        ExecutorService executorService = new ThreadPoolExecutor(
                2,
                3,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue(10)
        ) {
            //重写afterExecute方法
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                //这个是excute提交的时候
                if (t != null) {
                    System.out.println("afterExecute里面获取到excute提交的异常信息，处理异常" + t.getMessage());
                }
                //如果r的实际类型是FutureTask 那么是submit提交的，所以可以在里面get到异常
                if (r instanceof FutureTask) {
                    try {
                        Future<?> future = (Future<?>) r;
                        //get获取异常
                        future.get();
                    } catch (Exception e) {
                        System.out.println("afterExecute里面获取到submit提交的异常信息，处理异常" + e);
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }
        };
        //当线程池抛出异常后 execute
        executorService.execute(new Task3());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("==================为检验打印结果，1秒后执行execute方法");
        try {
            for (int i = 0; i < 10; i++) {
                executorService.submit(new Task3());
            }
            System.out.println("结束");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }
}

class Task3 implements Runnable {

    @Override
    public void run() {
        System.out.println("进入了task方法！！！");
        int i = 1 / 0;
    }
}
