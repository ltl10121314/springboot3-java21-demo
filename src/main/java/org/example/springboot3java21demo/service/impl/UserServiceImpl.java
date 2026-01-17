package org.example.springboot3java21demo.service.impl;

import jakarta.annotation.Resource;
import org.example.springboot3java21demo.domain.Student;
import org.example.springboot3java21demo.domain.User;
import org.example.springboot3java21demo.mapper.StudentDao;
import org.example.springboot3java21demo.mapper.UserDao;
import org.example.springboot3java21demo.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private StudentDao studentDao;

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Student insert(Student student) {
        studentDao.insert(student);
        return student;
    }

    @Override
    public List<User> findById(Map<String, Object> condition) {
        return userDao.findById(condition);
    }
}
