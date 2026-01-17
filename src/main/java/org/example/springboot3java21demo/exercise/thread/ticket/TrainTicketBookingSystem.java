package org.example.springboot3java21demo.exercise.thread.ticket;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1.使用synchronized关键字（线程安全）
 */
public class TrainTicketBookingSystem {
    private int availableTickets;
    private AtomicInteger ticketsSold = new AtomicInteger(0);

    public TrainTicketBookingSystem(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    // 同步方法，确保同一时间只有一个线程可以执行购票操作
    public synchronized boolean purchaseTicket(String user, int ticketsToBuy) {
        if (availableTickets >= ticketsToBuy) {
            // 模拟一些处理时间
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            availableTickets -= ticketsToBuy;
            int sold = ticketsSold.addAndGet(ticketsToBuy);
            System.out.println(user + " 成功购买了 " + ticketsToBuy + " 张票。已售出: " + sold + " 张，剩余: " + availableTickets + " 张");
            return true;
        } else {
            System.out.println(user + " 购票失败。余票不足。");
            return false;
        }
    }

    public static void main(String[] args) {
        TrainTicketBookingSystem bookingSystem = new TrainTicketBookingSystem(10); // 初始有10张票

        // 创建三个用户线程
        Thread user1 = new Thread(() -> {
            bookingSystem.purchaseTicket("用户A", 3);
        });

        Thread user2 = new Thread(() -> {
            bookingSystem.purchaseTicket("用户B", 4);
        });

        Thread user3 = new Thread(() -> {
            bookingSystem.purchaseTicket("用户C", 5);
        });

        // 同时启动三个线程
        user1.start();
        user2.start();
        user3.start();

        // 等待所有线程完成
        try {
            user1.join();
            user2.join();
            user3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("最终余票: " + bookingSystem.availableTickets);
    }
}
