package org.example.springboot3java21demo.exercise.sort;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 快速排序实现
 * <p>
 * 时间复杂度：平均 O(n log n)，最坏 O(n²)
 * 空间复杂度：O(log n)（递归栈空间）
 * <p>
 * 算法思想：
 * 1. 选择一个基准元素（pivot）
 * 2. 将数组分为两部分：小于基准的放左边，大于基准的放右边
 * 3. 递归地对左右两部分进行快速排序
 */
@Slf4j
public class QuickSort {

    /**
     * 快速排序入口方法
     *
     * @param arr 待排序数组
     */
    public static void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSort(arr, 0, arr.length - 1);
    }

    /**
     * 递归快速排序
     *
     * @param arr  待排序数组
     * @param low  起始索引
     * @param high 结束索引
     */
    private static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            // 获取分区点，pivot 已归位
            int pivotIndex = partition(arr, low, high);
            // 递归排序左半部分
            quickSort(arr, low, pivotIndex - 1);
            // 递归排序右半部分
            quickSort(arr, pivotIndex + 1, high);
        }
    }

    /**
     * 分区操作：选取最后一个元素作为基准
     *
     * @param arr  待分区数组
     * @param low  起始索引
     * @param high 结束索引
     * @return 基准元素的最终位置
     */
    private static int partition(int[] arr, int low, int high) {
        // 选择最后一个元素作为基准
        int pivot = arr[high];
        // i 指向小于基准的区域的最后一个位置
        int i = low - 1;

        for (int j = low; j < high; j++) {
            // 如果当前元素小于基准
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }

        // 将基准元素放到正确的位置
        swap(arr, i + 1, high);
        return i + 1;
    }

    /**
     * 交换数组中两个元素
     *
     * @param arr 数组
     * @param i   索引 i
     * @param j   索引 j
     */
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /**
     * 三数取中法选择基准，避免最坏情况
     *
     * @param arr  数组
     * @param low  起始索引
     * @param high 结束索引
     * @return 中间值的索引
     */
    private static int medianOfThree(int[] arr, int low, int high) {
        int mid = low + (high - low) / 2;

        // 找出 arr[low]、arr[mid]、arr[high] 的中位数
        if (arr[low] > arr[mid]) {
            swap(arr, low, mid);
        }
        if (arr[low] > arr[high]) {
            swap(arr, low, high);
        }
        if (arr[mid] > arr[high]) {
            swap(arr, mid, high);
        }

        // 将中位数放到 high-1 位置
        return mid;
    }

    /**
     * 优化版快速排序（三数取中 + 小数组使用插入排序）
     *
     * @param arr 待排序数组
     */
    public static void sortOptimized(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSortOptimized(arr, 0, arr.length - 1);
    }

    /**
     * 优化版递归快速排序
     */
    private static void quickSortOptimized(int[] arr, int low, int high) {
        // 小数组使用插入排序（阈值可调）
        if (high - low < 16) {
            insertionSort(arr, low, high);
            return;
        }

        if (low < high) {
            // 三数取中
            int medianIndex = medianOfThree(arr, low, high);
            swap(arr, medianIndex, high);

            int pivotIndex = partition(arr, low, high);
            quickSortOptimized(arr, low, pivotIndex - 1);
            quickSortOptimized(arr, pivotIndex + 1, high);
        }
    }

    /**
     * 对数组的指定范围进行插入排序
     */
    private static void insertionSort(int[] arr, int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= low && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    /**
     * 泛型快速排序（支持自定义比较器）
     *
     * @param <T>      泛型类型
     * @param arr      待排序数组
     * @param comparator 比较器
     */
    public static <T> void sort(T[] arr, java.util.Comparator<? super T> comparator) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSort(arr, 0, arr.length - 1, comparator);
    }

    private static <T> void quickSort(T[] arr, int low, int high, java.util.Comparator<? super T> comparator) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high, comparator);
            quickSort(arr, low, pivotIndex - 1, comparator);
            quickSort(arr, pivotIndex + 1, high, comparator);
        }
    }

    private static <T> int partition(T[] arr, int low, int high, java.util.Comparator<? super T> comparator) {
        T pivot = arr[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare(arr[j], pivot) <= 0) {
                i++;
                swap(arr, i, j);
            }
        }

        swap(arr, i + 1, high);
        return i + 1;
    }

    private static <T> void swap(T[] arr, int i, int j) {
        T temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试基本快速排序
        int[] arr = {64, 34, 25, 12, 22, 11, 90, 88, 45, 3};
        log.info("原始数组: {}", Arrays.toString(arr));

        int[] arr1 = arr.clone();
        QuickSort.sort(arr1);
        log.info("排序结果: {}", Arrays.toString(arr1));

        // 测试优化版快速排序
        int[] arr2 = arr.clone();
        QuickSort.sortOptimized(arr2);
        log.info("优化版排序结果: {}", Arrays.toString(arr2));

        // 测试泛型快速排序
        String[] strArr = {"banana", "apple", "orange", "grape", "pear"};
        log.info("原始字符串数组: {}", Arrays.toString(strArr));
        QuickSort.sort(strArr, String::compareTo);
        log.info("排序后字符串数组: {}", Arrays.toString(strArr));

        // 测试边界情况
        int[] emptyArr = {};
        int[] singleArr = {1};
        int[] sortedArr = {1, 2, 3, 4, 5};
        int[] reverseArr = {5, 4, 3, 2, 1};

        QuickSort.sort(emptyArr);
        QuickSort.sort(singleArr);
        QuickSort.sort(sortedArr);
        QuickSort.sort(reverseArr);

        log.info("空数组排序: {}", Arrays.toString(emptyArr));
        log.info("单元素数组排序: {}", Arrays.toString(singleArr));
        log.info("已排序数组排序: {}", Arrays.toString(sortedArr));
        log.info("逆序数组排序: {}", Arrays.toString(reverseArr));
    }
}
