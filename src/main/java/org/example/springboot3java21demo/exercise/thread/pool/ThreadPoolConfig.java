package org.example.springboot3java21demo.exercise.thread.pool;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 生产级线程池配置类
 * 根据业务场景区分 CPU 密集型和 IO 密集型线程池
 */
@Configuration
@EnableScheduling // 启用定时任务，供 ThreadPoolMonitor 使用
@ConfigurationProperties(prefix = "app.thread-pool") // 绑定 application.yml 中的配置
public class ThreadPoolConfig {

    // ========== CPU 密集型线程池参数 ==========
    private int cpuCoreSize = Runtime.getRuntime().availableProcessors();
    private int cpuMaxPoolSize = Runtime.getRuntime().availableProcessors();
    private int cpuQueueCapacity = 100;
    private int cpuKeepAliveSeconds = 60;

    // ========== IO 密集型线程池参数 ==========
    private int ioCoreSize = 20;
    private int ioMaxPoolSize = 50;
    private int ioQueueCapacity = 1000;
    private int ioKeepAliveSeconds = 60;

    // ========== 优雅关闭参数 ==========
    private boolean waitForTasksToCompleteOnShutdown = true;
    private int awaitTerminationSeconds = 30;

    /**
     * CPU 密集型线程池
     * 适用场景：复杂计算、数据加密、序列化等
     * 核心线程数 = CPU核心数，避免过多上下文切换
     */
    @Bean("cpuTaskExecutor")
    public ThreadPoolTaskExecutor cpuTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cpuCoreSize);
        executor.setMaxPoolSize(cpuMaxPoolSize);
        executor.setQueueCapacity(cpuQueueCapacity);
        executor.setKeepAliveSeconds(cpuKeepAliveSeconds);

        // 自定义线程命名，便于日志排查和监控
        executor.setThreadNamePrefix("cpu-pool-");
        executor.setThreadFactory(new CustomThreadFactory("cpu-pool"));

        // 拒绝策略：队列满时直接抛出异常，快速失败
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        // 优雅关闭配置
        executor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        executor.initialize();
        return executor;
    }

    /**
     * IO 密集型线程池
     * 适用场景：HTTP调用、数据库查询、文件读写等
     * 核心线程数 > CPU核心数，充分利用等待时间
     */
    @Bean("ioTaskExecutor")
    public ThreadPoolTaskExecutor ioTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ioCoreSize);
        executor.setMaxPoolSize(ioMaxPoolSize);
        executor.setQueueCapacity(ioQueueCapacity);
        executor.setKeepAliveSeconds(ioKeepAliveSeconds);

        // 自定义线程命名
        executor.setThreadNamePrefix("io-pool-");
        executor.setThreadFactory(new CustomThreadFactory("io-pool"));

        // 拒绝策略：队列满时由调用线程执行，起到削峰填谷作用
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 优雅关闭配置
        executor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        executor.initialize();
        return executor;
    }

    // ========== Getter/Setter 用于 @ConfigurationProperties 绑定 ==========
    public void setCpuCoreSize(int cpuCoreSize) { this.cpuCoreSize = cpuCoreSize; }
    public void setCpuMaxPoolSize(int cpuMaxPoolSize) { this.cpuMaxPoolSize = cpuMaxPoolSize; }
    public void setCpuQueueCapacity(int cpuQueueCapacity) { this.cpuQueueCapacity = cpuQueueCapacity; }
    public void setCpuKeepAliveSeconds(int cpuKeepAliveSeconds) { this.cpuKeepAliveSeconds = cpuKeepAliveSeconds; }
    public void setIoCoreSize(int ioCoreSize) { this.ioCoreSize = ioCoreSize; }
    public void setIoMaxPoolSize(int ioMaxPoolSize) { this.ioMaxPoolSize = ioMaxPoolSize; }
    public void setIoQueueCapacity(int ioQueueCapacity) { this.ioQueueCapacity = ioQueueCapacity; }
    public void setIoKeepAliveSeconds(int ioKeepAliveSeconds) { this.ioKeepAliveSeconds = ioKeepAliveSeconds; }
    public void setWaitForTasksToCompleteOnShutdown(boolean v) { this.waitForTasksToCompleteOnShutdown = v; }
    public void setAwaitTerminationSeconds(int v) { this.awaitTerminationSeconds = v; }
}
