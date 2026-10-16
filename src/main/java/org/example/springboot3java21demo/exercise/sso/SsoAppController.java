package org.example.springboot3java21demo.exercise.sso;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 业务系统 A / B（真实部署中是两个独立应用，这里用 /appA、/appB 前缀模拟）
 *
 * <p>每个系统有自己的本地会话（HttpSession），登录态由认证中心统一管理：</p>
 * <pre>
 * 访问 /appA/index -> 无本地会话 -> SsoLoginFilter 302 到认证中心
 * -> 登录成功跳回 /appA/callback?ticket=ST-xxx
 * -> 调 /sso/validate 校验票据（一次性）-> 建立本地会话 -> 跳回 /appA/index
 * </pre>
 */
@Slf4j
@RestController
public class SsoAppController {

    /** 认证中心地址（真实部署中是独立的 SSO 服务地址，可配置） */
    private final RestClient ssoClient;

    public SsoAppController(@Value("${sso.server-url:http://localhost:8081}") String ssoServerUrl) {
        this.ssoClient = RestClient.builder().baseUrl(ssoServerUrl).build();
    }

    // ==================== 系统 A ====================

    @GetMapping(value = "/appA/index", produces = "text/html;charset=UTF-8")
    public String appAIndex(HttpSession session) {
        return indexPage("系统A", "/appB/index", "系统B", user(session, "appA"));
    }

    @GetMapping("/appA/callback")
    public RedirectView appACallback(@RequestParam String ticket,
                                     @RequestParam(required = false) String redirect,
                                     HttpSession session) {
        return callback("appA", ticket, redirect, session);
    }

    @GetMapping("/appA/logoutCallback")
    public String appALogoutCallback(HttpSession session) {
        log.info("系统A 收到注销通知, 清理本地会话");
        session.invalidate();
        return "ok";
    }

    // ==================== 系统 B ====================

    @GetMapping(value = "/appB/index", produces = "text/html;charset=UTF-8")
    public String appBIndex(HttpSession session) {
        return indexPage("系统B", "/appA/index", "系统A", user(session, "appB"));
    }

    @GetMapping("/appB/callback")
    public RedirectView appBCallback(@RequestParam String ticket,
                                     @RequestParam(required = false) String redirect,
                                     HttpSession session) {
        return callback("appB", ticket, redirect, session);
    }

    @GetMapping("/appB/logoutCallback")
    public String appBLogoutCallback(HttpSession session) {
        log.info("系统B 收到注销通知, 清理本地会话");
        session.invalidate();
        return "ok";
    }

    /**
     * 票据回调：拿 ticket 去认证中心校验，通过后建立本地会话并跳回目标页面
     */
    private RedirectView callback(String appName, String ticket, String redirect, HttpSession session) {
        ValidateResult result = null;
        try {
            result = ssoClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/sso/validate").queryParam("ticket", ticket).build())
                    .retrieve()
                    .body(ValidateResult.class);
        } catch (Exception e) {
            log.error("[{}] 调用认证中心校验票据失败", appName, e);
        }
        if (result != null && result.code() == 0 && result.data() != null) {
            // 校验通过：建立本地会话，跳回原始目标页面
            session.setAttribute(SsoConstants.SESSION_USER_KEY_PREFIX + appName, result.data());
            log.info("[{}] 票据校验通过, 建立本地会话: {}", appName, result.data().username());
            return new RedirectView(redirect == null || redirect.isBlank() ? "/" + appName + "/index" : redirect);
        }
        // 票据无效：重新走认证中心（若 TGC 仍有效会直接免登录跳回）
        String callbackUrl = "/" + appName + "/callback";
        if (redirect != null && !redirect.isBlank()) {
            callbackUrl = callbackUrl + "?redirect=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8);
        }
        return new RedirectView("/sso/login?callbackUrl=" + URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8));
    }

    private SsoUser user(HttpSession session, String appName) {
        return (SsoUser) session.getAttribute(SsoConstants.SESSION_USER_KEY_PREFIX + appName);
    }

    /**
     * 业务系统首页 HTML
     */
    private String indexPage(String appName, String otherAppUrl, String otherAppName, SsoUser user) {
        String welcome = user == null
                ? "未登录"
                : "当前用户：" + user.username() + "（" + user.nickname() + "）";
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>{{APP}}首页</title>
                    <style>
                        body { font-family: "Microsoft YaHei", sans-serif; background: #f0f2f5; text-align: center; padding-top: 100px; color: #333; }
                        a { color: #409eff; text-decoration: none; margin: 0 8px; }
                    </style>
                </head>
                <body>
                <h2>欢迎来到{{APP}}</h2>
                <p>{{WELCOME}}</p>
                <p><a href="{{OTHER_URL}}">进入{{OTHER_APP}}</a><a href="/sso/logout">注销</a></p>
                </body>
                </html>
                """
                .replace("{{APP}}", appName)
                .replace("{{WELCOME}}", welcome)
                .replace("{{OTHER_URL}}", otherAppUrl)
                .replace("{{OTHER_APP}}", otherAppName);
    }
}
