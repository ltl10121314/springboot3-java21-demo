package org.example.springboot3java21demo.exercise.thread.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控组件
 * 定期输出线程池运行指标，用于生产环境观测
 * 注意：@EnableScheduling 统一放在 ThreadPoolConfig 配置类上，组件类不重复声明
 */
@Component
public class ThreadPoolMonitor {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolMonitor.class);

    /**
     * 队列使用率告警阈值
     */
    private static final double QUEUE_USAGE_WARN_THRESHOLD = 0.8;

    private final ThreadPoolTaskExecutor cpuExecutor;
    private final ThreadPoolTaskExecutor ioExecutor;

    public ThreadPoolMonitor(@Qualifier("cpuTaskExecutor") ThreadPoolTaskExecutor cpuExecutor, @Qualifier("ioTaskExecutor") ThreadPoolTaskExecutor ioExecutor) {
        this.cpuExecutor = cpuExecutor;
        this.ioExecutor = ioExecutor;
    }

    /**
     * 每 30 秒输出一次线程池指标，可通过 app.thread-pool.monitor-interval-ms 覆盖
     * 生产环境建议接入 Prometheus + Grafana 做可视化监控
     */
    @Scheduled(fixedRateString = "${app.thread-pool.monitor-interval-ms:30000}")
    public void monitorThreadPool() {
        logMetrics("CPU", cpuExecutor);
        logMetrics("IO", ioExecutor);
    }

    /**
     * 输出单个线程池的运行指标，队列积压过高时额外输出告警
     *
     * @param poolName 线程池名称（用于日志前缀）
     * @param executor 目标线程池
     */
    private void logMetrics(String poolName, ThreadPoolTaskExecutor executor) {
        ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
        int queueSize = pool.getQueue().size();
        int queueCapacity = executor.getQueueCapacity();
        // 队列容量可能未配置，做除零保护
        int queueUsagePercent = queueCapacity > 0
                ? (int) ((double) queueSize / queueCapacity * 100)
                : 0;
        int active = executor.getActiveCount();
        int poolSize = executor.getPoolSize();

        log.info("[{}线程池] 核心={}, 当前线程={}, 最大={}, 活跃={}, 队列={}/{} ({}%), 已完成={}",
                poolName,
                executor.getCorePoolSize(),
                poolSize,
                executor.getMaxPoolSize(),
                active,
                queueSize,
                queueCapacity,
                queueUsagePercent,
                pool.getCompletedTaskCount()
        );

        // 队列积压过高说明任务生产速度超过消费速度，需要扩容或降级
        if (queueUsagePercent >= QUEUE_USAGE_WARN_THRESHOLD * 100) {
            log.warn("[{}线程池] 队列积压过高! 使用率={}%, 活跃线程={}/{}",
                    poolName, queueUsagePercent, active, poolSize);
        }
    }
}