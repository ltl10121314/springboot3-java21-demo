package org.example.springboot3java21demo.exercise.redis;

import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Set;

/**
 * REDIS 测试类
 */
public class RedisTest {

    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            // jedis.auth("123456");
            // PING测试
            System.out.println(jedis.ping());
        }
    }

    @Test
    public void test1() {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            //set keys
            jedis.set("k1", "v1");
            jedis.set("k2", "v2");
            jedis.set("k3", "v3");
        }
    }

    @Test
    public void test2() {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            //get key
            System.out.println(jedis.get("k1"));
        }
    }

    @Test
    public void test3() {
        Set<String> keys;
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            //keys *
            keys = jedis.keys("*");
        }
        //for keys
        for (String key : keys) {
            System.out.println("key: " + key);
        }
        //key size
        System.out.println("key size: " + keys.size());
    }

    @Test
    public void test4() {
        long startTime;
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            // jedis.auth("123456");
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                jedis.set("k2" + i, i + "");
            }
        }
        long endTime = System.currentTimeMillis();
        long usedTime = endTime - startTime;
        System.out.println(usedTime + "ms");
    }

    @Test
    public void test5() {
        long startTime;
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            // jedis.auth("123456");
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                Pipeline pipelined = jedis.pipelined();
                for (int j = i * 100; j < (i + 1) * 100; j++) {
                    pipelined.set("k3" + i, i + "");
                }
                pipelined.sync();
            }
        }
        long endTime = System.currentTimeMillis();
        long usedTime = (endTime - startTime);
        System.out.println(usedTime + "ms");
    }

    @Test
    public void test6() throws InterruptedException {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            List<Object> exec = null;
            while (exec == null) {
                System.out.println(jedis.watch("k1"));
                Transaction transaction = jedis.multi();
                Thread.sleep(3000);
                transaction.set("k1", "v2");
                exec = transaction.exec();
                System.out.println(exec);
                Thread.sleep(2000);
            }
        }
    }
}
