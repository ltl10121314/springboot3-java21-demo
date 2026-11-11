package org.example.springboot3java21demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户表 {@code user}。
 */
@TableName("user")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    /** 用户主键。 */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /** 用户业务编码，可用于外部人员或账号编号关联。 */
    @TableField("code")
    private String code;

    /** 用户名称。 */
    @TableField("name")
    private String name;

    /** 用户年龄；现有表使用字符串类型保存。 */
    @TableField("age")
    private String age;

    /** 用户地址。 */
    @TableField("address")
    private String address;
}
