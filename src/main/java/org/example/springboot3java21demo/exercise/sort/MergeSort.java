package org.example.springboot3java21demo.exercise.sort;

import java.util.Arrays;

/**
 * 归并排序实现
 * 归并排序是一种高效的排序算法，采用分治法策略
 * 时间复杂度：O(n log n)
 * 空间复杂度：O(n)
 * 稳定性：稳定排序
 */
public class MergeSort {

    /**
     * 归并排序主方法
     *
     * @param array 待排序数组
     */
    public static void mergeSort(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        int[] temp = new int[array.length];
        mergeSort(array, 0, array.length - 1, temp);
    }

    /**
     * 递归分解数组
     *
     * @param array 原始数组
     * @param left  左边界
     * @param right 右边界
     * @param temp  临时数组
     */
    private static void mergeSort(int[] array, int left, int right, int[] temp) {
        if (left >= right) {
            return;
        }

        // 计算中间位置
        int mid = left + (right - left) / 2;

        // 递归分解左半部分
        mergeSort(array, left, mid, temp);

        // 递归分解右半部分
        mergeSort(array, mid + 1, right, temp);

        // 合并两个有序子数组
        merge(array, left, mid, right, temp);
    }

    /**
     * 合并两个有序子数组
     *
     * @param array 原始数组
     * @param left  左边界
     * @param mid   中间位置
     * @param right 右边界
     * @param temp  临时数组
     */
    private static void merge(int[] array, int left, int mid, int right, int[] temp) {
        // 左子数组的起始位置
        int i = left;
        // 右子数组的起始位置
        int j = mid + 1;
        // 临时数组的索引
        int t = 0;

        // 比较两个子数组的元素，将较小的放入临时数组
        while (i <= mid && j <= right) {
            if (array[i] <= array[j]) {
                temp[t++] = array[i++];
            } else {
                temp[t++] = array[j++];
            }
        }

        // 将左子数组剩余元素复制到临时数组
        while (i <= mid) {
            temp[t++] = array[i++];
        }

        // 将右子数组剩余元素复制到临时数组
        while (j <= right) {
            temp[t++] = array[j++];
        }

        // 将临时数组中的元素复制回原数组
        t = 0;
        int index = left;
        while (index <= right) {
            array[index++] = temp[t++];
        }
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试用例 1：无序数组
        int[] array1 = {38, 27, 43, 3, 9, 82, 10};
        System.out.println("原始数组 1: " + Arrays.toString(array1));
        mergeSort(array1);
        System.out.println("排序结果 1: " + Arrays.toString(array1));

        // 测试用例 2：已排序数组
        int[] array2 = {1, 2, 3, 4, 5, 6, 7};
        System.out.println("\n原始数组 2: " + Arrays.toString(array2));
        mergeSort(array2);
        System.out.println("排序结果 2: " + Arrays.toString(array2));

        // 测试用例 3：逆序数组
        int[] array3 = {7, 6, 5, 4, 3, 2, 1};
        System.out.println("\n原始数组 3: " + Arrays.toString(array3));
        mergeSort(array3);
        System.out.println("排序结果 3: " + Arrays.toString(array3));

        // 测试用例 4：包含重复元素
        int[] array4 = {5, 2, 8, 2, 9, 1, 5, 5};
        System.out.println("\n原始数组 4: " + Arrays.toString(array4));
        mergeSort(array4);
        System.out.println("排序结果 4: " + Arrays.toString(array4));

        // 测试用例 5：空数组
        int[] array5 = {};
        System.out.println("\n原始数组 5: " + Arrays.toString(array5));
        mergeSort(array5);
        System.out.println("排序结果 5: " + Arrays.toString(array5));

        // 测试用例 6：单元素数组
        int[] array6 = {42};
        System.out.println("\n原始数组 6: " + Arrays.toString(array6));
        mergeSort(array6);
        System.out.println("排序结果 6: " + Arrays.toString(array6));
    }
}
