package org.example.springboot3java21demo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "REDIS控制类", description = "调用REDIS相关接口")
@Slf4j
@RestController
@RequestMapping("redis")
public class RedisController {

    @Resource
    private RedisUtils redisUtils;

    @RequestMapping(value = "setAndGet", method = RequestMethod.GET)
    public String setAndGet(String k, String v) {
        redisUtils.set(k, v);
        return (String) redisUtils.get(k);
    }
}
