package org.example.springboot3java21demo.exercise.sort;

import java.util.Arrays;
import java.util.Random;

/**
 * 快速排序与归并排序的 Java 实现（含随机测试）。
 */
public class SortDemo {

    /* ===================== 快速排序 ===================== */

    /**
     * 快速排序（递归版）。对 arr 原地排序，默认升序。
     * <p>
     * 时间复杂度：平均 O(n log n)，最坏（已有序且固定基准）O(n^2)。
     * 空间复杂度：递归栈 O(log n)（平均）。
     */
    public static void quickSort(int[] arr) {
        if (arr == null || arr.length <= 1) return;
        quickSort(arr, 0, arr.length - 1);
    }

    private static void quickSort(int[] arr, int left, int right) {
        if (left >= right) return;
        int pivotIndex = partition(arr, left, right);
        quickSort(arr, left, pivotIndex - 1);
        quickSort(arr, pivotIndex + 1, right);
    }

    /**
     * 分区：取区间最后一个元素为基准，将小于等于基准的放到左侧。
     * 返回基准最终的位置。
     */
    private static int partition(int[] arr, int left, int right) {
        int pivot = arr[right];
        int i = left; // i 指向「已处理的小于等于区」的下一个位置
        for (int j = left; j < right; j++) {
            if (arr[j] <= pivot) {
                swap(arr, i, j);
                i++;
            }
        }
        swap(arr, i, right); // 把基准放到分界点
        return i;
    }

    /**
     * 快速排序（三数取中 + 随机基准的稳健写法）。
     * 通过比较首、中、尾取中位数作为基准，降低退化概率。
     */
    public static void quickSortRobust(int[] arr) {
        if (arr == null || arr.length <= 1) return;
        quickSortRobust(arr, 0, arr.length - 1);
    }

    private static void quickSortRobust(int[] arr, int left, int right) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        // 三数取中，把中位数换到最右作为基准
        int pivotIndex = medianOfThree(arr, left, mid, right);
        swap(arr, pivotIndex, right);
        int p = partition(arr, left, right);
        quickSortRobust(arr, left, p - 1);
        quickSortRobust(arr, p + 1, right);
    }

    private static int medianOfThree(int[] arr, int a, int b, int c) {
        if (arr[a] <= arr[b]) {
            if (arr[b] <= arr[c]) return b;
            return arr[a] <= arr[c] ? c : a;
        } else {
            if (arr[a] <= arr[c]) return a;
            return arr[b] <= arr[c] ? c : b;
        }
    }

    /* ===================== 归并排序 ===================== */

    /**
     * 归并排序（递归版）。稳定，时间复杂度始终 O(n log n)。
     * 需要 O(n) 额外空间。
     */
    public static void mergeSort(int[] arr) {
        if (arr == null || arr.length <= 1) return;
        int[] temp = new int[arr.length]; // 复用临时数组，避免每层都 new
        mergeSort(arr, temp, 0, arr.length - 1);
    }

    private static void mergeSort(int[] arr, int[] temp, int left, int right) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        mergeSort(arr, temp, left, mid);
        mergeSort(arr, temp, mid + 1, right);
        merge(arr, temp, left, mid, right);
    }

    /**
     * 合并两个有序子区间 [left, mid] 与 [mid+1, right]。
     */
    private static void merge(int[] arr, int[] temp, int left, int mid, int right) {
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right) {
            // 等号保证稳定性：左侧相等元素先放入
            temp[k++] = arr[i] <= arr[j] ? arr[i++] : arr[j++];
        }
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        // 把排好序的部分拷回原数组
        for (int p = left; p <= right; p++) arr[p] = temp[p];
    }

    /**
     * 归并排序（自底向上迭代版）。无递归，避免递归栈溢出风险。
     */
    public static void mergeSortBottomUp(int[] arr) {
        if (arr == null || arr.length <= 1) return;
        int n = arr.length;
        int[] temp = new int[n];
        for (int width = 1; width < n; width *= 2) {
            for (int left = 0; left < n; left += 2 * width) {
                int mid = Math.min(left + width, n) - 1;
                int right = Math.min(left + 2 * width, n) - 1;
                if (mid >= right) continue; // 只有一段，已有序
                merge(arr, temp, left, mid, right);
            }
        }
    }

    /* ===================== 工具方法 ===================== */

    private static void swap(int[] arr, int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    private static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Random rnd = new Random(42);
        String[] names = {
                "快速排序(末位基准)", "快速排序(三数取中)",
                "归并排序(递归)", "归并排序(自底向上)"
        };

        for (int t = 0; t < 5; t++) {
            int n = 20;
            int[] original = rnd.ints(n, 0, 100).toArray();

            int[] a = original.clone();
            quickSort(a);
            int[] b = original.clone();
            quickSortRobust(b);
            int[] c = original.clone();
            mergeSort(c);
            int[] d = original.clone();
            mergeSortBottomUp(d);

            System.out.println("第 " + (t + 1) + " 组，随机数组：" + Arrays.toString(original));
            System.out.println("  " + names[0] + " -> " + Arrays.toString(a) + " 正确=" + isSorted(a));
            System.out.println("  " + names[1] + " -> " + Arrays.toString(b) + " 正确=" + isSorted(b));
            System.out.println("  " + names[2] + " -> " + Arrays.toString(c) + " 正确=" + isSorted(c));
            System.out.println("  " + names[3] + " -> " + Arrays.toString(d) + " 正确=" + isSorted(d));
            System.out.println();
        }
    }
}
