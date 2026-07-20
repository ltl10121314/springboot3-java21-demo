package org.example.springboot3java21demo.exercise.stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class DequeExample {
    private static final Logger log = LoggerFactory.getLogger(DequeExample.class);
    public static void main(String[] args) {
        // 1. 当作栈使用 (LIFO) - 推荐写法
        Deque<String> stack = new ArrayDeque<>();
        stack.push("A");
        stack.push("B");
        stack.push("C");
        log.info(stack.pop()); // 输出 C（后进先出）
        log.info(stack.peek()); // 输出 B（查看栈顶）

        // 2. 当作队列使用 (FIFO)
        Deque<Integer> queue = new ArrayDeque<>();
        queue.offer(1);  // 队尾入队
        queue.offer(2);
        log.info("{}",queue.poll()); // 输出 1（先进先出）
        log.info("{}",queue.peek()); // 输出 2

        // 3. 双端操作（从两端取数据）
        Deque<String> deque = new ArrayDeque<>();
        deque.addFirst("头部元素");
        deque.addLast("尾部元素");
        log.info(deque.getFirst()); // 头部元素
        log.info(deque.getLast());  // 尾部元素

        // 正序
        for (String s : deque) {
            log.info(s);
        }
        log.error("--------------");
        Iterator<String> iterator = deque.descendingIterator();
        while (iterator.hasNext()) {
            log.error(iterator.next());
        }
    }
}
