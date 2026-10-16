package org.example.springboot3java21demo.exercise.sso;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * SSO 统一认证中心（真实部署中是独立的认证服务，这里用 /sso 前缀在同一应用内模拟）
 *
 * <p>核心流程（CAS 风格）：</p>
 * <pre>
 * 1. 业务系统未登录         -> 302 跳转 /sso/login?callbackUrl=业务系统回调地址
 * 2. 认证中心校验账号密码    -> 创建全局会话 -> 写 TGC cookie -> 签发一次性票据 ST
 * 3. 302 跳回 callbackUrl?ticket=ST-xxx
 * 4. 业务系统拿 ticket 调 /sso/validate 校验 -> 建立本地会话
 * 5. 再访问其他业务系统时，凭 TGC 直接签发新票据，无需重新登录（单点登录）
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
public class SsoController {

    private final SsoSessionManager sessionManager;

    /**
     * 登录页；若已持有全局会话（TGC）则免登录，直接签发票据跳回业务系统
     */
    @GetMapping(value = "/login", produces = "text/html;charset=UTF-8")
    public Object loginPage(@RequestParam(required = false) String callbackUrl,
                            @RequestParam(required = false, defaultValue = "0") int error,
                            HttpServletRequest request) {
        if (callbackUrl == null || callbackUrl.isBlank()) {
            callbackUrl = "/appA/index";
        }
        String tgc = cookieValue(request, SsoConstants.TGC_COOKIE);
        if (tgc != null && sessionManager.getUserByTgc(tgc) != null) {
            // 已登录：直接签发票据，实现免登录
            String ticket = sessionManager.issueTicket(tgc);
            log.info("TGC 有效, 免登录签发票据跳回: {}", callbackUrl);
            return new RedirectView(appendParam(callbackUrl, "ticket", ticket));
        }
        return loginHtml(callbackUrl, error);
    }

    /**
     * 登录提交：校验账号密码 -> 创建全局会话 -> 写 TGC cookie -> 签发票据 -> 跳回业务系统
     */
    @PostMapping("/login")
    public RedirectView login(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam(required = false) String callbackUrl,
                              HttpServletResponse response) {
        if (callbackUrl == null || callbackUrl.isBlank()) {
            callbackUrl = "/appA/index";
        }
        if (!sessionManager.checkUser(username, password)) {
            log.info("登录失败, username:{}", username);
            return new RedirectView("/sso/login?error=1&callbackUrl="
                    + URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8));
        }
        // 1. 创建全局会话，返回 TGC
        String tgc = sessionManager.createGlobalSession(username);
        // 2. 写 TGC cookie，代表用户在认证中心的登录态（真实系统中作用域为认证中心域名）
        ResponseCookie tgcCookie = ResponseCookie.from(SsoConstants.TGC_COOKIE, tgc)
                .path("/")
                .maxAge(SsoConstants.GLOBAL_SESSION_TTL / 1000)
                .httpOnly(true)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, tgcCookie.toString());
        // 3. 签发一次性服务票据，跳回业务系统回调
        String ticket = sessionManager.issueTicket(tgc);
        log.info("登录成功, username:{}, 跳回: {}", username, callbackUrl);
        return new RedirectView(appendParam(callbackUrl, "ticket", ticket));
    }

    /**
     * 票据校验接口：供业务系统服务端调用，一次性使用
     */
    @GetMapping("/validate")
    public ValidateResult validate(@RequestParam String ticket) {
        SsoUser user = sessionManager.validateTicket(ticket);
        if (user == null) {
            return new ValidateResult(1, "票据无效或已过期", null);
        }
        return new ValidateResult(0, "ok", user);
    }

    /**
     * 注销：销毁全局会话，清除 TGC，并通知各业务系统清理本地会话
     */
    @GetMapping(value = "/logout", produces = "text/html;charset=UTF-8")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        String tgc = cookieValue(request, SsoConstants.TGC_COOKIE);
        if (tgc != null) {
            sessionManager.destroyGlobalSession(tgc);
        }
        // 清除 TGC cookie
        ResponseCookie tgcCookie = ResponseCookie.from(SsoConstants.TGC_COOKIE, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, tgcCookie.toString());
        // 通过 iframe 加载各业务系统登记的注销回调，清理本地会话
        StringBuilder notify = new StringBuilder();
        for (String logoutUrl : SsoConstants.REGISTERED_LOGOUT_URLS) {
            notify.append("<iframe src=\"").append(logoutUrl).append("\" style=\"display:none\"></iframe>");
        }
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>注销成功</title>
                    <style>
                        body { font-family: "Microsoft YaHei", sans-serif; background: #f0f2f5; text-align: center; padding-top: 100px; color: #333; }
                        a { color: #409eff; text-decoration: none; margin: 0 8px; }
                    </style>
                </head>
                <body>
                {{NOTIFY}}
                <h2>已注销</h2>
                <p>全局会话已销毁，各业务系统本地会话已清理</p>
                <p><a href="/appA/index">重新访问系统A</a><a href="/appB/index">重新访问系统B</a></p>
                </body>
                </html>
                """
                .replace("{{NOTIFY}}", notify.toString());
    }

    /**
     * 登录页 HTML
     */
    private String loginHtml(String callbackUrl, int error) {
        String errorMsg = error == 1 ? "<p style=\"color:#f56c6c;text-align:center;\">用户名或密码错误</p>" : "";
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>统一认证中心</title>
                    <style>
                        body { font-family: "Microsoft YaHei", sans-serif; background: #f0f2f5; display: flex; justify-content: center; padding-top: 80px; }
                        .box { background: #fff; padding: 32px 40px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,.08); width: 360px; }
                        h2 { text-align: center; color: #333; margin-bottom: 24px; }
                        input { width: 100%; box-sizing: border-box; padding: 10px 12px; margin-bottom: 16px; border: 1px solid #dcdfe6; border-radius: 4px; font-size: 14px; }
                        button { width: 100%; padding: 10px; background: #409eff; color: #fff; border: none; border-radius: 4px; font-size: 15px; cursor: pointer; }
                        button:hover { background: #66b1ff; }
                        .tip { color: #909399; font-size: 12px; text-align: center; margin-top: 12px; }
                    </style>
                </head>
                <body>
                <div class="box">
                    <h2>统一认证中心</h2>
                    {{ERROR}}
                    <form method="post" action="/sso/login">
                        <input type="hidden" name="callbackUrl" value="{{CALLBACK}}">
                        <input type="text" name="username" placeholder="用户名" required>
                        <input type="password" name="password" placeholder="密码" required>
                        <button type="submit">登 录</button>
                    </form>
                    <div class="tip">演示账号：admin / 123456，zhangsan / 123456</div>
                </div>
                </body>
                </html>
                """
                .replace("{{ERROR}}", errorMsg)
                .replace("{{CALLBACK}}", escapeHtml(callbackUrl));
    }

    /**
     * 往 url 追加参数（自动判断已有 ? 的情况）
     */
    private String appendParam(String url, String name, String value) {
        return url + (url.contains("?") ? "&" : "?") + name + "=" + value;
    }

    private String cookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
