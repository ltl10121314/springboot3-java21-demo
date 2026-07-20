package org.example.springboot3java21demo.exercise.sort;

public class Solution {

    public static void main(String[] args) {
        ListNode head = new ListNode(0);
        head.next = new ListNode(1);
        head.next.next = new ListNode(2);
        head.next.next.next = new ListNode(3);
        head.next.next.next.next = new ListNode(4);
        head.next.next.next.next.next = new ListNode(5);
        head.next.next.next.next.next.next = new ListNode(6);
        ListNode reverseList = reverseKGroup(head, 2);
        while (reverseList != null) {
            System.out.print(reverseList.val + " ");
            reverseList = reverseList.next;
        }
    }

    /**
     * 代码中的类名、方法名、参数名已经指定，请勿修改，直接返回方法规定的值即可
     *
     * @param head ListNode类
     * @param k    int整型
     * @return ListNode类
     */
    public static ListNode reverseKGroup(ListNode head, int k) {
        // write code here
        ListNode tail = head;
        for (int i = 0; i < k; i++) {
            if (tail == null) {
                return head;
            }
            tail = tail.next;
        }

        return head;
    }

    public static ListNode ReverseList(ListNode head) {
        // write code here
        ListNode tail = null;
        while (head != null) {
            ListNode temp = head;
            head = head.next;
            temp.next = tail;
            tail = temp;
        }
        return tail;
    }

    public static class ListNode {
        int val;
        ListNode next = null;

        public ListNode(int val) {
            this.val = val;
        }
    }
}


