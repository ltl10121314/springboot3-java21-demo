package org.example.springboot3java21demo.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.springboot3java21demo.common.web.WebRequestSupport;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 仅在 API Controller 请求进入时建立 traceId。
 * Actuator、静态资源和后台定时任务不会因此生成 traceId。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTraceIdFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return WebRequestSupport.isInfrastructureRequest(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String traceId = TraceContext.sanitize(
                request.getHeader(TraceConstants.TRACE_ID_HEADER));
        if (traceId == null) {
            traceId = TraceContext.newTraceId();
        }
        response.setHeader(TraceConstants.TRACE_ID_HEADER, traceId);
        try (TraceContext.Scope ignored = TraceContext.openIfPresent(traceId)) {
            filterChain.doFilter(request, response);
        }
    }
}
