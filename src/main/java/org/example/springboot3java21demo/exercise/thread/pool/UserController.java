package org.example.springboot3java21demo.exercise.thread.pool;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 异步聚合接口
 * <p>提供同步和异步两种响应模式
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AsyncAggregationService aggregationService;

    public UserController(AsyncAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    /**
     * 模式一：同步等待
     * 适用于前端直接获取结果的场景
     * 注意：会占用 Servlet 线程直到异步任务完成
     */
    @GetMapping("/aggregate/sync")
    public String aggregateSync(@RequestParam String userId) throws Exception {
        // 阻塞等待异步结果，最多等待 6 秒
        return aggregationService.aggregateUserData(userId)
                .get(6, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * 模式二：异步响应（推荐）
     * 使用 DeferredResult 释放 Servlet 线程
     * 适用于高并发场景，避免线程耗尽
     */
    @GetMapping("/aggregate/async")
    public DeferredResult<String> aggregateAsync(@RequestParam String userId) {
        // 设置 6 秒超时
        DeferredResult<String> deferredResult = new DeferredResult<>(6000L);

        aggregationService.aggregateUserData(userId)
                .thenAccept(result -> deferredResult.setResult(result))
                .exceptionally(ex -> {
                    deferredResult.setErrorResult("服务异常: " + ex.getMessage());
                    return null;
                });

        // 设置超时回调
        deferredResult.onTimeout(() ->
                deferredResult.setErrorResult("请求超时，请稍后重试")
        );

        return deferredResult;
    }
}
