package org.example.springboot3java21demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 学生实体
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Student {
//    private static final long serialVersionUID = -91969758749726312L;
    /**
     * 唯一标识id
     */
    private String id;
    /**
     * 姓名
     */
    private String name;
    /**
     * 年龄
     */
    private Integer age;

    private Date beginDate;

    private Date endDate;
}
