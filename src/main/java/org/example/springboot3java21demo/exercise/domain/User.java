package org.example.springboot3java21demo.exercise.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class User {
    public String id;
    private String code;
    private String name;
    private String age;
    private String address;
    private int orderId;
    private Date creationTime;
    private User children;

    {
        log.info("方法块");
    }

    static {
        log.info("静态方法块");
    }

    public User(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
        log.info("构造方法");
    }
}
