package org.example.springboot3java21demo.exercise.domain;

import lombok.Data;

@Data
public class Student implements Comparable<Student> {

    private int id;
    private String name;
    private int age;

    @Override
    public int compareTo(Student o) {
        return Integer.compare(this.age, o.age);
    }
}