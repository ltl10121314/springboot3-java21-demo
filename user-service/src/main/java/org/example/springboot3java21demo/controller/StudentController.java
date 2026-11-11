package org.example.springboot3java21demo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.common.utils.ResponseUtils;
import org.example.springboot3java21demo.common.utils.ResultMessage;
import org.example.springboot3java21demo.domain.Student;
import org.example.springboot3java21demo.model.StudentParamVO;
import org.example.springboot3java21demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "学生控制类", description = "调用学生相关接口")
@Slf4j
@RestController
@RequestMapping("/api/students")
public class StudentController {

    /**
     * 服务对象
     */
    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id       主键
     * @param response 响应
     */
    @GetMapping("selectOne")
    public void selectOne(@RequestParam("id") String id, HttpServletResponse response) {
        log.info("查询学生, id:{}", id);
        try {
            ResponseUtils.renderJson(response, ResultMessage.data(studentService.queryById(id)));
        } catch (Exception e) {
            log.error("查询学生，返回 JSON 失败, id:{}", id, e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 分页查询
     *
     * @param offset   查询起始位置
     * @param limit    查询条数
     * @param response 响应
     */
    @GetMapping("queryAllByLimit")
    public void queryAllByLimit(@RequestParam(defaultValue = "0") int offset,
                                @RequestParam(defaultValue = "10") int limit,
                                HttpServletResponse response) {
        log.info("分页查询学生, offset:{}, limit:{}", offset, limit);
        try {
            ResponseUtils.renderJson(response, ResultMessage.data(studentService.queryAllByLimit(offset, limit)));
        } catch (Exception e) {
            log.error("分页查询学生，返回 JSON 失败, offset:{}, limit:{}", offset, limit, e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 新增学生
     *
     * @param student  学生信息
     * @param response 响应
     */
    @PostMapping("insert")
    public void insert(@RequestBody Student student, HttpServletResponse response) {
        log.info("新增学生, student:{}", student);
        try {
            ResponseUtils.renderJson(response, ResultMessage.data(studentService.insert(student)));
        } catch (Exception e) {
            log.error("新增学生，返回 JSON 失败, student:{}", student, e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 修改数据
     *
     * @param student  学生信息
     * @param response 响应
     */
    @PostMapping("update")
    public void update(@RequestBody Student student, HttpServletResponse response) {
        log.info("修改学生, student:{}", student);
        try {
            ResponseUtils.renderJson(response, ResultMessage.data(studentService.update(student)));
        } catch (Exception e) {
            log.error("修改学生，返回 JSON 失败, student:{}", student, e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 通过主键删除数据
     *
     * @param id       主键
     * @param response 响应
     */
    @DeleteMapping("deleteById")
    public void deleteById(@RequestParam("id") String id, HttpServletResponse response) {
        log.info("删除学生, id:{}", id);
        try {
            ResponseUtils.renderJson(response, ResultMessage.data(studentService.deleteById(id)));
        } catch (Exception e) {
            log.error("删除学生，返回 JSON 失败, id:{}", id, e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 测试事务回滚
     *
     * @param student  学生信息
     * @param response 响应
     */
    @PostMapping("updateAndInsert")
    public void updateAndInsert(@RequestBody Student student, HttpServletResponse response) {
        log.info("测试事务回滚, student:{}", student);
        studentService.updateAndInsert(student);
        try {
            ResponseUtils.renderJson(response, ResultMessage.success());
        } catch (Exception e) {
            log.error("测试事务回滚，返回 JSON 失败, student:{}", student, e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 按条件查询学生
     *
     * @param paramVO  查询条件
     * @param response 响应
     */
    @PostMapping("queryStudentByCondition")
    public void queryStudentByCondition(@RequestBody StudentParamVO paramVO, HttpServletResponse response) {
        log.info("按条件查询学生, paramVO:{}", paramVO);
        try {
            ResponseUtils.renderJson(response, ResultMessage.data(studentService.queryStudentByCondition(paramVO)));
        } catch (Exception e) {
            log.error("按条件查询学生，返回 JSON 失败, paramVO:{}", paramVO, e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 测试事务传播行为
     *
     * @param response 响应
     */
    @GetMapping("task")
    public void task(HttpServletResponse response) {
        studentService.task();
        try {
            ResponseUtils.renderJson(response, ResultMessage.success());
        } catch (Exception e) {
            log.error("测试事务传播行为，返回 JSON 失败", e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 测试事务传播行为（内层方法）
     *
     * @param response 响应
     */
    @GetMapping("task1")
    public void task1(HttpServletResponse response) {
        studentService.task1();
        try {
            ResponseUtils.renderJson(response, ResultMessage.success());
        } catch (Exception e) {
            log.error("测试事务传播行为（task1），返回 JSON 失败", e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 测试事务传播行为（NOT_SUPPORTED，方法内部会抛异常）
     *
     * @param response 响应
     */
    @GetMapping("task2")
    public void task2(HttpServletResponse response) {
        studentService.task2();
        try {
            ResponseUtils.renderJson(response, ResultMessage.success());
        } catch (Exception e) {
            log.error("测试事务传播行为（task2），返回 JSON 失败", e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    @GetMapping("hello")
    public String hello() {
        return "hello";
    }

    @PostMapping(value = {"/open-api/bankOffer/insertTemplateForIReport", "/internal/open-api/bankOffer/insertTemplateForIReport"})
    public void insertTemplateForIReport(HttpServletResponse response, @RequestBody Map<String, Object> param) {
        Object waBankOfferTemplate = param.get("wa_bank_offer_template");
        if (waBankOfferTemplate instanceof List) {
            List<Map<String, Object>> mapList = (List<Map<String, Object>>) waBankOfferTemplate;
            System.out.println("----------------");
        }
        System.out.println("0000000000000");
    }
}
