package org.example.springboot3java21demo.orderpay.web;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.example.springboot3java21demo.common.web.ApiErrorLoggingFilter;
import org.example.springboot3java21demo.orderpay.exception.OrderPayException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPayExceptionHandlerTest {

    private final OrderPayExceptionHandler handler = new OrderPayExceptionHandler();
    private final Logger logger = (Logger) LoggerFactory.getLogger(OrderPayExceptionHandler.class);
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
    @DisplayName("支付业务异常记录请求路径和业务错误码")
    void logsBusinessExceptionWithRequestContext() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/api/order-pay/orders/ORD-404/pay");

        var response = handler.handleBusiness(
                new OrderPayException(404, 404, "订单不存在"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("订单支付业务异常")
                        && message.contains("code=404")
                        && message.contains("/api/order-pay/orders/ORD-404/pay"));
        assertThat(request.getAttribute(ApiErrorLoggingFilter.ERROR_LOGGED_ATTRIBUTE))
                .isEqualTo(Boolean.TRUE);
    }

    @Test
    @DisplayName("未预期异常记录完整堆栈和请求路径")
    void logsUnexpectedExceptionWithStackTrace() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/api/order-pay/orders/ORD-500/pay");

        var response = handler.handleUnexpected(
                new IllegalStateException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(appender.list)
                .filteredOn(event -> event.getThrowableProxy() != null)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("订单支付接口异常")
                        && message.contains("IllegalStateException")
                        && message.contains("boom"));
    }
}
