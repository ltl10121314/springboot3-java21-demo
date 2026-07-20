package org.example.springboot3java21demo.exercise.sort;

import java.util.*;

public class OdTest001 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // 报文数量C
        int C = Integer.parseInt(sc.nextLine());

        /*
         * key：router发起者
         * value：router发起者的多个host接受者
         */
        Map<String, HashSet<String>> map = new HashMap<>();

        // 后续C行依次输入设备节点D1和D2
        for (int i = 0; i < C; i++) {
            String[] arr = sc.nextLine().split(" ");
            // 发送查询报文
            String router = arr[0];
            // 发送响应报文
            String host = arr[1];
            map.putIfAbsent(router, new HashSet<>());
            map.putIfAbsent(host, new HashSet<>());
            map.get(router).add(host);
        }

        // 遍历关系集合，获取router发起者
        for (String send : map.keySet()) {
            // 获取router发起者的多个host接受者
            for (String receive : map.get(send)) {
                /*
                 * 获取“host接受者”的host接受者，比较是否“礼尚往来”
                 * 我给你了，你没还我，直接断绝关系，返回false
                 */
                if (!map.get(receive).contains(send)) {
                    System.out.println("fasle");
                    return;
                }
            }
        }
        // 礼尚往来，和和美美一家人
        System.out.println("true");
    }
}

