package org.example.springboot3java21demo.exercise.domain;

import lombok.*;

import java.util.Date;

/**
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    private String id;
    private String code;
    private String name;
    private String age;
    private String address;
    private int orderId;
    private Date creationTime;
    private User children;

    {
        System.out.println("方法块");
    }

    static {
        System.out.println("静态方法块");
    }

    public User(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
        System.out.println("构造方法");
    }
}
