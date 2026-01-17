package org.example.springboot3java21demo.exercise.thread;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 
 */
public class CallableThreadTest implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallableThreadTest.class);

    public static void main(String[] args) {
        CallableThreadTest ctt = new CallableThreadTest();
        FutureTask<Integer> ft = new FutureTask<>(ctt);
        for (int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + " 的循环变量i的值" + i);
            if (i == 20) {
                new Thread(ft, "有返回值的线程").start();
            }
        }

        try {
            System.out.println("子线程的返回值：" + ft.get());
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("", e);
        }

    }

    @Override
    public Integer call() throws Exception {
        int i = 0;
        for (; i < 100; i++) {
            LOGGER.error("{} {}", Thread.currentThread().getName(), i);
        }
        return i;
    }

    @Test
    public void testInBlockedIOState() throws InterruptedException {
        final Scanner in = new Scanner(System.in);
        // 创建一个名为“输入输出”的线程t
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 命令行中的阻塞读
                    String input = in.nextLine();
                    System.out.println(input);
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
            // 线程的名字
        }, "输入输出");

        // 启动
        t.start();

        // 确保run已经得到执行
        Thread.sleep(100);

        // 状态为RUNNABLE
        assert (t.getState()).equals(Thread.State.RUNNABLE);
    }

    @Test
    public void testBlockedSocketState() throws Exception {
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(10086);
                    while (true) {
                        // 阻塞的accept方法
                        Socket socket = serverSocket.accept();
                        // TODO
                    }
                } catch (IOException e) {
                    LOGGER.error("", e);
                } finally {
                    try {
                        if (serverSocket != null) {
                            serverSocket.close();
                        }
                    } catch (IOException e) {
                        LOGGER.error("", e);
                    }
                }
            }
            // 线程的名字
        }, "socket线程");
        serverThread.start();

        // 确保run已经得到执行
        Thread.sleep(500);

        // 状态为RUNNABLE
        assert (serverThread.getState()).equals(Thread.State.RUNNABLE);

    }

}

