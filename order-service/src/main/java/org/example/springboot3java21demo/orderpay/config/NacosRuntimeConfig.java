package org.example.springboot3java21demo.orderpay.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Nacos 运行时规则。Dubbo 使用 Nacos 做注册中心，这里再读取配置中心，
 * 演示业务参数不重启即可生效。
 */
@Slf4j
@Component
public class NacosRuntimeConfig {

    private static final String DEFAULT_CONTENT = """
            order.timeout-seconds=120
            """;

    private final OrderPayProperties properties;
    private final AtomicReference<RuntimeRules> rules = new AtomicReference<>();
    private final Executor listenerExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "order-pay-nacos-listener");
        thread.setDaemon(true);
        return thread;
    });

    private volatile ConfigService configService;
    private volatile Listener configListener;
    private volatile String status = "NOT_STARTED";
    private volatile String lastError;

    public NacosRuntimeConfig(OrderPayProperties properties) {
        this.properties = properties;
        this.rules.set(defaultRules());
    }

    @PostConstruct
    public void start() {
        OrderPayProperties.Nacos nacos = properties.getNacos();
        if (!nacos.isEnabled()) {
            status = "DISABLED";
            return;
        }
        try {
            Properties clientProperties = new Properties();
            clientProperties.setProperty(PropertyKeyConst.SERVER_ADDR, nacos.getServerAddress());
            if (nacos.getNamespace() != null && !nacos.getNamespace().isBlank()) {
                clientProperties.setProperty(PropertyKeyConst.NAMESPACE, nacos.getNamespace());
            }
            configService = NacosFactory.createConfigService(clientProperties);
            String content = configService.getConfig(
                    nacos.getDataId(), nacos.getGroup(), nacos.getTimeoutMillis());
            if (content == null || content.isBlank()) {
                boolean published = configService.publishConfig(
                        nacos.getDataId(), nacos.getGroup(), DEFAULT_CONTENT);
                log.info("初始化 Nacos 订单支付配置 dataId={}, published={}", nacos.getDataId(), published);
                content = configService.getConfig(
                        nacos.getDataId(), nacos.getGroup(), nacos.getTimeoutMillis());
            }
            apply(content);
            configListener = new Listener() {
                @Override
                public Executor getExecutor() {
                    return listenerExecutor;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    apply(configInfo);
                    log.info("Nacos 订单支付配置已热更新 dataId={}", nacos.getDataId());
                }
            };
            configService.addListener(nacos.getDataId(), nacos.getGroup(), configListener);
            status = "CONNECTED";
        } catch (Exception ex) {
            status = "FALLBACK";
            lastError = ex.getMessage();
            log.error("连接 Nacos 配置中心失败，暂时使用本地默认值 dataId={}",
                    nacos.getDataId(), ex);
        }
    }

    public int timeoutSeconds() {
        return rules.get().timeoutSeconds();
    }

    public String status() {
        return status;
    }

    public String lastError() {
        return lastError;
    }

    @PreDestroy
    public void stop() {
        if (configService == null) {
            return;
        }
        try {
            if (configListener != null) {
                configService.removeListener(
                        properties.getNacos().getDataId(),
                        properties.getNacos().getGroup(),
                        configListener);
            }
            configService.shutDown();
        } catch (Exception ex) {
            log.debug("关闭 Nacos ConfigService 失败", ex);
        }
    }

    private void apply(String content) {
        if (content == null || content.isBlank()) {
            rules.set(defaultRules());
            return;
        }
        Properties values = new Properties();
        try {
            values.load(new StringReader(content));
            int timeout = Integer.parseInt(values.getProperty(
                    "order.timeout-seconds", String.valueOf(properties.getTimeoutSeconds())));
            rules.set(new RuntimeRules(Math.max(1, timeout), Instant.now()));
        } catch (Exception ex) {
            lastError = ex.getMessage();
            log.error("Nacos 配置格式错误，保留上一版配置 content={}", content, ex);
        }
    }

    private RuntimeRules defaultRules() {
        return new RuntimeRules(Math.max(1, properties.getTimeoutSeconds()), Instant.now());
    }

    public record RuntimeRules(int timeoutSeconds, Instant loadedAt) {
    }
}
