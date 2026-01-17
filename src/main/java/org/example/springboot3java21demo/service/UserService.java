package org.example.springboot3java21demo.service;

import org.example.springboot3java21demo.domain.Student;
import org.example.springboot3java21demo.domain.User;

import java.util.List;
import java.util.Map;

/**
 * 用户逻辑层接口
 */
public interface UserService {

    /**
     * 获取所有用户信息
     *
     * @return
     */
    List<User> findAll();

    /**
     * 插入学生逻辑
     *
     * @param student 学生
     * @return
     */
    Student insert(Student student);

    /**
     * 批量查询用户
     *
     * @param condition
     * @return
     */
    List<User> findById(Map<String, Object> condition);
}
