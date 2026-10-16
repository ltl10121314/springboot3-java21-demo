package org.example.springboot3java21demo.exercise.sso;

import java.util.List;

/**
 * SSO 相关常量
 */
public final class SsoConstants {

    private SsoConstants() {
    }

    /** 全局会话 cookie 名称（TGC：Ticket Granting Cookie，代表用户在认证中心的登录态） */
    public static final String TGC_COOKIE = "TGC";

    /** 全局会话有效期：1 小时 */
    public static final long GLOBAL_SESSION_TTL = 60 * 60 * 1000L;

    /** 服务票据（ST）有效期：5 分钟 */
    public static final long TICKET_TTL = 5 * 60 * 1000L;

    /** 业务系统本地会话中存放用户信息的 key 前缀（每个系统独立，避免互相串号） */
    public static final String SESSION_USER_KEY_PREFIX = "SSO_USER_";

    /**
     * 已在认证中心登记的业务系统注销回调地址
     * 真实系统中由各业务系统在认证中心注册，认证中心注销时逐个通知清理本地会话
     */
    public static final List<String> REGISTERED_LOGOUT_URLS = List.of(
            "/appA/logoutCallback",
            "/appB/logoutCallback"
    );
}
