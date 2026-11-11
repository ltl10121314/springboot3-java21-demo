package org.example.springboot3java21demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 学生/成员档案表 {@code student}。
 */
@TableName("student")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Student {
//    private static final long serialVersionUID = -91969758749726312L;
    /** 学生或成员档案主键。 */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /** 学生或成员姓名。 */
    @TableField("name")
    private String name;

    /** 学生或成员年龄。 */
    @TableField("age")
    private Integer age;

    /** 当前档案记录的开始时间，数据库默认使用创建时间。 */
    @TableField("beginDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginDate;

    /** 当前档案记录的结束时间，现有表会随记录更新自动刷新。 */
    @TableField("endDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;
}
