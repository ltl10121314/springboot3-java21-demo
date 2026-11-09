package org.example.springboot3java21demo.mapper;

import org.example.springboot3java21demo.domain.Student;
import org.example.springboot3java21demo.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户 DAO
 */
@Mapper
public interface UserDao {

    /**
     * 查询所有用户
     *
     * @return 返回用户实体
     */
    @Select("select * from `user`")
    List<User> findAll();

    /**
     * 插入用户信息
     *
     * @param user 用户
     * @return 插入用户数量
     */
     Integer insert(User user);

    /**
     * 根据ID批量查询用户信息
     *
     * @param condition 条件
     * @return 用户信息
     */
    List<User> findById(@Param("condition") Map<String, Object> condition);

    /**
     * 根据ID查询用户
     *
     * @param condition 条件
     * @return 返回
     */
    User findUserById(@Param("condition") Map<String, Object> condition);
}
