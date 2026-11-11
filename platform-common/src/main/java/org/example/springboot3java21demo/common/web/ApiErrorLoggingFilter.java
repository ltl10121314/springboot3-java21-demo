package org.example.springboot3java21demo.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API 错误日志兜底：任何 /api 请求最终返回 4xx/5xx 时都必须留痕。
 * 异常处理器已经记录过时通过请求属性去重。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ApiErrorLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiErrorLoggingFilter.class);

    public static final String ERROR_LOGGED_ATTRIBUTE =
            ApiErrorLoggingFilter.class.getName() + ".ERROR_LOGGED";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return WebRequestSupport.isInfrastructureRequest(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            request.setAttribute(ERROR_LOGGED_ATTRIBUTE, Boolean.TRUE);
            log.error("API 请求发生未处理异常 method={}, uri={}, exception={}, elapsedMs={}",
                    request.getMethod(), request.getRequestURI(),
                    ex.getClass().getSimpleName(), elapsedMillis(startedAt), ex);
            throw ex;
        } finally {
            int status = response.getStatus();
            if (status >= 400 && !Boolean.TRUE.equals(request.getAttribute(ERROR_LOGGED_ATTRIBUTE))) {
                request.setAttribute(ERROR_LOGGED_ATTRIBUTE, Boolean.TRUE);
                if (status >= 500) {
                    log.error("API 请求失败 method={}, uri={}, status={}, elapsedMs={}",
                            request.getMethod(), request.getRequestURI(), status,
                            elapsedMillis(startedAt));
                } else {
                    log.warn("API 请求失败 method={}, uri={}, status={}, elapsedMs={}",
                            request.getMethod(), request.getRequestURI(), status,
                            elapsedMillis(startedAt));
                }
            }
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
