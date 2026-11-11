package org.example.springboot3java21demo.paymentservice.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class NacosPaymentConfig {

    private static final String DEFAULT_CONTENT = """
            payment.allowed-channels=ALIPAY,WECHAT,MOCK
            """;

    private final PaymentServiceProperties properties;
    private final AtomicReference<Set<String>> allowedChannels = new AtomicReference<>();
    private final Executor listenerExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "payment-nacos-listener");
        thread.setDaemon(true);
        return thread;
    });

    private volatile ConfigService configService;
    private volatile Listener configListener;
    private volatile String status = "NOT_STARTED";
    private volatile String lastError;

    public NacosPaymentConfig(PaymentServiceProperties properties) {
        this.properties = properties;
        this.allowedChannels.set(properties.getGateway().getAllowedChannels());
    }

    @PostConstruct
    public void start() {
        PaymentServiceProperties.Nacos nacos = properties.getNacos();
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
                configService.publishConfig(nacos.getDataId(), nacos.getGroup(), DEFAULT_CONTENT);
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
                    log.info("Nacos 支付服务配置已热更新 dataId={}", nacos.getDataId());
                }
            };
            configService.addListener(nacos.getDataId(), nacos.getGroup(), configListener);
            status = "CONNECTED";
        } catch (Exception ex) {
            status = "FALLBACK";
            lastError = ex.getMessage();
            log.error("连接 Nacos 支付配置失败，使用本地渠道配置 dataId={}", nacos.getDataId(), ex);
        }
    }

    public boolean channelAllowed(String channel) {
        return allowedChannels.get().contains(channel);
    }

    public Set<String> allowedChannels() {
        return Set.copyOf(allowedChannels.get());
    }

    public String status() {
        return status;
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
            return;
        }
        try {
            Properties values = new Properties();
            values.load(new StringReader(content));
            Set<String> channels = new LinkedHashSet<>(Arrays.stream(
                            values.getProperty("payment.allowed-channels", "ALIPAY,WECHAT,MOCK")
                                    .split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .map(String::toUpperCase)
                    .toList());
            allowedChannels.set(Set.copyOf(channels));
        } catch (Exception ex) {
            lastError = ex.getMessage();
            log.error("Nacos 支付服务配置格式错误 content={}", content, ex);
        }
    }
}
