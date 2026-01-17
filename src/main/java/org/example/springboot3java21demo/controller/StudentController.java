package org.example.springboot3java21demo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.example.springboot3java21demo.domain.Student;
import org.example.springboot3java21demo.service.StudentService;
import org.example.springboot3java21demo.service.UserService;
import org.example.springboot3java21demo.utils.SpringContextUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "学生控制类", description = "调用学生相关接口")
@Slf4j
@RestController
@RequestMapping("student")
public class StudentController {
    /**
     * 服务对象
     */
    @Resource
    private StudentService studentService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public Student selectOne(@Param("id") String id) {
        log.info("hello");
        SpringContextUtil.getBean(UserService.class).findAll();
        return SpringContextUtil.getBean(StudentService.class).queryById(id);
    }

    @GetMapping("hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("task")
    public void task() {
        studentService.task();
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