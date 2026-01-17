package org.example.springboot3java21demo.exercise.generic;

import org.junit.Test;

import java.util.*;

/**
 * 
 * @date 2020/6/28 23:04
 * @description
 */
public class ArrayListTest {
    @Test
    public void arrayListTest() {
        ArrayList<String> objects = new ArrayList<String>();
        objects.add("apple");
        objects.add("pear");
        objects.add("banana");
        Iterator<String> iterator = objects.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
            iterator.remove();
        }
        System.out.println(objects);
    }

    @Test
    public void arrayList2Test() {
        Student s = new Student("Xiao Ming", 99);
        Map<String, Student> map = new HashMap<String, Student>();
        // 将"Xiao Ming"和Student实例映射并关联
        map.put("Xiao Ming", s);
        // 通过key查找并返回映射的Student实例
        Student target = map.get("Xiao Ming");
        // true，同一个实例
        System.out.println(target == s);
        // 99
        System.out.println(target.score);
        // 通过另一个key查找
        Student another = map.get("Bob");
        // 未找到返回null
        System.out.println(another);
    }

    @Test
    public void arrayList3Test() {

    }
}

class Students {
    List<Student> list;
    Map<String, Integer> cache;

    Students(List<Student> list) {
        this.list = list;
        cache = new HashMap<String, Integer>();
    }

    /**
     * 根据name查找score，找到返回score，未找到返回-1
     */
    int getScore(String name) {
        // 先在Map中查找:
        Integer score = this.cache.get(name);
        if (score == null) {
            // TODO:
        }
        return score == null ? -1 : score.intValue();
    }

}

class Student {
    String name;
    int score;

    Student(String name, int score) {
        this.name = name;
        this.score = score;
    }
}