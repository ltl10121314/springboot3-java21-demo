package org.example.springboot3java21demo.service.impl;

import org.example.springboot3java21demo.domain.Student;
import org.example.springboot3java21demo.log.MyLog;
import org.example.springboot3java21demo.mapper.StudentDao;
import org.example.springboot3java21demo.model.StudentParamVO;
import org.example.springboot3java21demo.service.StudentService;
import org.example.springboot3java21demo.service.UserService;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService, BeanNameAware {

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    @MyLog
    public Student queryById(String id) {
        return studentDao.queryById(id);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<Student> queryAllByLimit(int offset, int limit) {
        return studentDao.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param student 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Student insert(Student student) {
        studentDao.insert(student);
        studentDao.update(student);
        return student;
    }

    /**
     * 修改数据
     *
     * @param student 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Student update(Student student) {
        studentDao.update(student);
        return queryById(student.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Student updateStudent(Student student) {
        Student student3 = new Student();
        student3.setId("3");
        student3.setAge(44);
        student3.setName("李四");
//        userService.insert(student3);
        studentService.insert(student3);
        return queryById(student.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String id) {
        return studentDao.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAndInsert(Student student) {
        Student student1 = new Student();
        student1.setId("11");
        student1.setAge(77);
        student1.setName("第一邪皇");
        Student student2 = new Student();
        student2.setId("12");
        student2.setAge(88);
        student2.setName("第二刀皇");
        studentService.update(student2);
        this.updateStudent(student2);
        studentService.insert(student1);
    }

    @Override
    public List<Student> queryStudentByCondition(StudentParamVO paramVO) {
        return studentDao.queryStudentByCondition(paramVO);
    }

    @Override
    @Transactional
    public void task() {
        studentDao.deleteAll();
        this.task1();
    }

    @Transactional(rollbackFor = Exception.class)
    public void task1() {
        Student student = new Student();
        student.setId("1");
        student.setAge(1);
        student.setName("第一邪皇");
        studentDao.insert(student);
        studentService.task2();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
    public void task2() {
        Student student = new Student();
        student.setId("2");
        student.setAge(2);
        student.setName("第二邪皇");
        studentDao.insert(student);
        task3();
        this.task4();
    }

    @Transactional(rollbackFor = Exception.class)
    public void task3() {
        Student student = new Student();
        student.setId("3");
        student.setAge(3);
        student.setName("第三邪皇");
        studentDao.insert(student);
    }

    private void task4() {
        try {
            int i = 1 / 0;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("beanName:" + name);
    }
}
