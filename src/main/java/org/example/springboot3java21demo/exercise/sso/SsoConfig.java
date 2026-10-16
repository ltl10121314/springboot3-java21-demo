package org.example.springboot3java21demo.exercise.sso;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SSO 演示配置：为两个业务系统分别注册登录过滤器
 * （真实部署中每个业务系统独立部署，过滤器随各自应用启动，互不相干）
 */
@Configuration
public class SsoConfig {

    @Bean
    public FilterRegistrationBean<SsoLoginFilter> appALoginFilterRegistration() {
        return loginFilterRegistration(new SsoLoginFilter("appA"), "/appA/*", 1);
    }

    @Bean
    public FilterRegistrationBean<SsoLoginFilter> appBLoginFilterRegistration() {
        return loginFilterRegistration(new SsoLoginFilter("appB"), "/appB/*", 2);
    }

    private FilterRegistrationBean<SsoLoginFilter> loginFilterRegistration(SsoLoginFilter filter,
                                                                           String urlPattern,
                                                                           int order) {
        FilterRegistrationBean<SsoLoginFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns(urlPattern);
        registration.setOrder(order);
        return registration;
    }
}
