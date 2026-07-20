package org.example.springboot3java21demo.exercise.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 矩阵相交面积
 */
public class OdTest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Rectangle> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            // 矩形的位置，分别代表"左上角x坐标"、"左上角y坐标"、"向右w"、"向下h"
//            int[] arr = Arrays.stream(sc.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            String[] line = sc.nextLine().split(" ");
            // x坐标
            int x = Integer.parseInt(line[0]);
            // y坐标
            int y = Integer.parseInt(line[1]);
            // 向右w
            int w = Integer.parseInt(line[2]);
            // 向下h
            int h = Integer.parseInt(line[3]);
            // 修正：创建矩形时参数顺序为(x1, x2, y1, y2)
            list.add(new Rectangle(x, x + w, y - h, y));
        }
        // 计算矩形相交
        Rectangle intersect = intersect(intersect(list.get(0), list.get(1)), list.get(2));
        if (intersect == null) {
            System.out.println(0);
        } else {
            // 修正：面积计算公式
            System.out.println((intersect.x2 - intersect.x1) * (intersect.y2 - intersect.y1));
        }
    }

    // 计算矩形相交
    public static Rectangle intersect(Rectangle r1, Rectangle r2) {
        if (r1 == null || r2 == null) {
            return null;
        }
        Rectangle rectangle = new Rectangle();
        // 修正：取最大x1（左边界取最右的）
        rectangle.x1 = Math.max(r1.x1, r2.x1);
        // 修正：取最小x2（右边界取最左的）
        rectangle.x2 = Math.min(r1.x2, r2.x2);
        // 修正：取最大y1（下边界取最上的）
        rectangle.y1 = Math.max(r1.y1, r2.y1);
        // 修正：取最小y2（上边界取最下的）
        rectangle.y2 = Math.min(r1.y2, r2.y2);
        // 修正：判断是否有交集
        if (rectangle.x1 >= rectangle.x2 || rectangle.y1 >= rectangle.y2) {
            return null;
        }
        return rectangle;
    }
}

/**
 * 矩形
 */
class Rectangle {
    public int x1; // 左边界
    public int x2; // 右边界
    public int y1; // 下边界
    public int y2; // 上边界

    public Rectangle() {
    }

    public Rectangle(int x1, int x2, int y1, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }
}

