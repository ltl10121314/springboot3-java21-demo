package org.example.springboot3java21demo.exercise.sso;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 业务系统登录过滤器：本地会话不存在时，携带回调地址跳转认证中心登录
 *
 * <p>系统A、系统B各注册一个实例（见 {@link SsoConfig}），session key 相互独立，
 * 模拟两个独立部署的业务系统。真实部署中该过滤器随各自的业务系统应用启动。</p>
 */
@Slf4j
public class SsoLoginFilter extends OncePerRequestFilter {

    private final String appName;

    /** 本地会话中存放用户信息的 key，每个系统独立 */
    private final String sessionUserKey;

    public SsoLoginFilter(String appName) {
        this.appName = appName;
        this.sessionUserKey = SsoConstants.SESSION_USER_KEY_PREFIX + appName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        // 票据回调、注销回调无需登录
        if (uri.endsWith("/callback") || uri.endsWith("/logoutCallback")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 已有本地会话，直接放行
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(sessionUserKey) != null) {
            filterChain.doFilter(request, response);
            return;
        }
        // 未登录：跳转认证中心，携带本系统票据回调地址（回调里再带原始目标地址）
        String originalUrl = request.getRequestURL().toString();
        if (request.getQueryString() != null) {
            originalUrl = originalUrl + "?" + request.getQueryString();
        }
        String callbackUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + "/" + appName + "/callback?redirect=" + URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
        String redirect = "/sso/login?callbackUrl=" + URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8);
        log.info("[{}] 无本地会话, 跳转认证中心: {}", appName, redirect);
        response.sendRedirect(redirect);
    }
}
