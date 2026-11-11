package org.example.springboot3java21demo.common.trace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTraceIdFilterTest {

    @AfterEach
    void clearTrace() {
        TraceContext.clear();
    }

    @Test
    @DisplayName("任意 Controller 路径都继承请求 traceId")
    void appliesToAllBusinessControllerPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.addHeader(TraceConstants.TRACE_ID_HEADER, "user-request-trace");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> traceInsideChain = new AtomicReference<>();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(
                    jakarta.servlet.ServletRequest ignored,
                    jakarta.servlet.ServletResponse ignoredResponse) {
                traceInsideChain.set(TraceContext.current());
            }
        };

        new RequestTraceIdFilter().doFilter(request, response, chain);

        assertThat(traceInsideChain).hasValue("user-request-trace");
        assertThat(response.getHeader(TraceConstants.TRACE_ID_HEADER))
                .isEqualTo("user-request-trace");
        assertThat(TraceContext.current()).isNull();
    }

    @Test
    @DisplayName("Actuator 请求不生成 traceId")
    void ignoresInfrastructurePaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> traceInsideChain = new AtomicReference<>("not-called");
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(
                    jakarta.servlet.ServletRequest ignored,
                    jakarta.servlet.ServletResponse ignoredResponse) {
                traceInsideChain.set(TraceContext.current());
            }
        };

        new RequestTraceIdFilter().doFilter(request, response, chain);

        assertThat(traceInsideChain).hasValue(null);
        assertThat(response.getHeader(TraceConstants.TRACE_ID_HEADER)).isNull();
    }
}
