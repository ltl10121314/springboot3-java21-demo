package org.example.springboot3java21demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体
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
}
