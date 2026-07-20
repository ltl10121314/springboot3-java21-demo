package org.example.springboot3java21demo.exercise.stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class DequeExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(DequeExample.class);
    public static void main(String[] args) {
        // 1. 当作栈使用 (LIFO) - 推荐写法
        Deque<String> stack = new ArrayDeque<>();
        stack.push("A");
        stack.push("B");
        stack.push("C");
        System.out.println(stack.pop()); // 输出 C（后进先出）
        System.out.println(stack.peek()); // 输出 B（查看栈顶）

        // 2. 当作队列使用 (FIFO)
        Deque<Integer> queue = new ArrayDeque<>();
        queue.offer(1);  // 队尾入队
        queue.offer(2);
        System.out.println(queue.poll()); // 输出 1（先进先出）
        System.out.println(queue.peek()); // 输出 2

        // 3. 双端操作（从两端取数据）
        Deque<String> deque = new ArrayDeque<>();
        deque.addFirst("头部元素");
        deque.addLast("尾部元素");
        System.out.println(deque.getFirst()); // 头部元素
        System.out.println(deque.getLast());  // 尾部元素

        // 正序
        for (String s : deque) {
            System.out.println(s);
        }
        LOGGER.error("--------------");
        Iterator<String> iterator = deque.descendingIterator();
        while (iterator.hasNext()) {
            LOGGER.error(iterator.next());
        }
    }
}
