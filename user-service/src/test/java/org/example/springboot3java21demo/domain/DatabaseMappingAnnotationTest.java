package org.example.springboot3java21demo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseMappingAnnotationTest {

    @Test
    @DisplayName("用户字段映射到 user 表")
    void mapsUserColumns() throws NoSuchFieldException {
        assertThat(User.class.getAnnotation(TableName.class).value())
                .isEqualTo("user");
        assertThat(User.class.getDeclaredField("id").getAnnotation(TableId.class).value())
                .isEqualTo("id");
        assertThat(User.class.getDeclaredField("address").getAnnotation(TableField.class).value())
                .isEqualTo("address");
    }

    @Test
    @DisplayName("学生字段映射到 student 表")
    void mapsStudentColumns() throws NoSuchFieldException {
        assertThat(Student.class.getAnnotation(TableName.class).value())
                .isEqualTo("student");
        assertThat(Student.class.getDeclaredField("id").getAnnotation(TableId.class).value())
                .isEqualTo("id");
        assertThat(Student.class.getDeclaredField("beginDate")
                .getAnnotation(TableField.class).value())
                .isEqualTo("beginDate");
    }
}
