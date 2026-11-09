package org.example.springboot3java21demo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.common.utils.ResponseUtils;
import org.example.springboot3java21demo.common.utils.ResultMessage;
import org.example.springboot3java21demo.domain.User;
import org.example.springboot3java21demo.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户控制类", description = "调用用户相关接口")
@Slf4j
@RestController
@RequestMapping(value = "user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 查询所有用户信息
     *
     * @param response 响应
     */
    @GetMapping("findAll")
    public void findAll(HttpServletResponse response) {
        log.info("查询所有用户信息");
        try {
            ResponseUtils.renderJson(response, ResultMessage.data(userService.findAll()));
        } catch (Exception e) {
            log.error("查询所有用户信息，返回 JSON 失败", e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 新增用户
     *
     * @param user     用户信息
     * @param response 响应
     */
    @PostMapping("insert")
    public void insert(@RequestBody User user, HttpServletResponse response) {
        log.info("新增用户, user:{}", user);
        try {
            ResponseUtils.renderJson(response, ResultMessage.data(userService.insert(user)));
        } catch (Exception e) {
            log.error("新增用户，返回 JSON 失败, user:{}", user, e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }

    /**
     * 根据条件批量查询用户
     *
     * @param condition 查询条件，如 {"ids": ["1", "2"]}
     * @param response  响应
     */
    @PostMapping("findById")
    public void findById(@RequestBody Map<String, Object> condition, HttpServletResponse response) {
        log.info("批量查询用户, condition:{}", condition);
        try {
            ResponseUtils.renderJson(response, ResultMessage.data(userService.findById(condition)));
        } catch (Exception e) {
            log.error("批量查询用户，返回 JSON 失败, condition:{}", condition, e);
            ResponseUtils.renderJson(response, ResultMessage.error(500L, e.getMessage()));
        }
    }
}
