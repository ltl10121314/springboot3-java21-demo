package org.example.springboot3java21demo.mapper;

import org.example.springboot3java21demo.domain.Student;
import org.example.springboot3java21demo.model.StudentParamVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 学生 DAO
 */
@Mapper
public interface StudentDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Student queryById(String id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<Student> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param student 实例对象
     * @return 对象列表
     */
    List<Student> queryAll(Student student);

    /**
     * 根据条件查询
     *
     * @param param 入参
     * @return 返回
     */
    List<Student> queryStudentByCondition(@Param("param") StudentParamVO param);

    /**
     * 新增数据
     *
     * @param student 实例对象
     * @return 影响行数
     */
    int insert(Student student);

    /**
     * 修改数据
     *
     * @param student 实例对象
     * @return 影响行数
     */
    int update(Student student);

    /**
     * 批量更新
     *
     * @param tableName 表名
     * @param values    值
     * @return 返回
     */
    int batchUpdate(@Param("tableName") String tableName, @Param("values") List<Map<String, Object>> values);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    Integer deleteById(String id);

    @Select("select * from `student`")
    List<Student> findAll();

    void deleteAll();

}

