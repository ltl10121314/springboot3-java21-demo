package org.example.springboot3java21demo.common.web;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorLoggingFilterTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ApiErrorLoggingFilter.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    {
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("API 请求返回 4xx 时必须记录错误日志")
    void logsClientErrorResponse() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/not-exists");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req,
                                 jakarta.servlet.ServletResponse res) {
                ((jakarta.servlet.http.HttpServletResponse) res).setStatus(404);
            }
        };

        new ApiErrorLoggingFilter().doFilter(request, response, chain);

        assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("API 请求失败")
                        && message.contains("status=404"));
        assertThat(request.getAttribute(ApiErrorLoggingFilter.ERROR_LOGGED_ATTRIBUTE))
                .isEqualTo(Boolean.TRUE);
    }

    @Test
    @DisplayName("异常处理器已记录时兜底过滤器不重复记录")
    void doesNotDuplicateAlreadyLoggedError() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/api/order-pay/orders/1/pay");
        request.setAttribute(ApiErrorLoggingFilter.ERROR_LOGGED_ATTRIBUTE, Boolean.TRUE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req,
                                 jakarta.servlet.ServletResponse res) {
                ((jakarta.servlet.http.HttpServletResponse) res).setStatus(500);
            }
        };

        new ApiErrorLoggingFilter().doFilter(request, response, chain);

        assertThat(appender.list).isEmpty();
    }
}
