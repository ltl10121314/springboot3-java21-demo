package org.example.springboot3java21demo.exercise.sso;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证中心会话管理：全局会话（TGC）与服务票据（ST）
 * 真实系统中通常存放在 Redis 等共享存储，这里用内存 Map 模拟
 */
@Slf4j
@Component
public class SsoSessionManager {

    /** 模拟的用户库，真实系统应查询数据库 */
    private static final Map<String, String> USER_STORE = Map.of(
            "admin", "123456",
            "zhangsan", "123456"
    );

    /** 用户名 -> 昵称 */
    private static final Map<String, String> NICKNAME_STORE = Map.of(
            "admin", "管理员",
            "zhangsan", "张三"
    );

    /** 全局会话：tgc -> 会话信息 */
    private final Map<String, GlobalSession> globalSessions = new ConcurrentHashMap<>();

    /** 服务票据：ticket -> 票据信息（一次性使用） */
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();

    /**
     * 校验用户名密码
     */
    public boolean checkUser(String username, String password) {
        return USER_STORE.containsKey(username) && USER_STORE.get(username).equals(password);
    }

    /**
     * 创建全局会话，返回 TGC
     */
    public String createGlobalSession(String username) {
        String tgc = "TGC-" + UUID.randomUUID();
        globalSessions.put(tgc, new GlobalSession(tgc, username, System.currentTimeMillis()));
        log.info("创建全局会话, tgc:{}, username:{}", tgc, username);
        return tgc;
    }

    /**
     * 根据 TGC 获取登录用户，会话不存在或已过期返回 null
     */
    public SsoUser getUserByTgc(String tgc) {
        GlobalSession session = globalSessions.get(tgc);
        if (session == null || session.expired()) {
            return null;
        }
        return buildUser(session.username);
    }

    /**
     * 为业务系统签发一次性服务票据 ST
     */
    public String issueTicket(String tgc) {
        GlobalSession session = globalSessions.get(tgc);
        if (session == null || session.expired()) {
            return null;
        }
        String ticket = "ST-" + UUID.randomUUID();
        tickets.put(ticket, new Ticket(ticket, tgc, session.username, System.currentTimeMillis()));
        log.info("签发服务票据, ticket:{}, username:{}", ticket, session.username);
        return ticket;
    }

    /**
     * 校验服务票据：一次性使用，校验后立即销毁
     */
    public SsoUser validateTicket(String ticket) {
        Ticket t = tickets.remove(ticket);
        if (t == null || t.expired()) {
            log.info("票据校验失败, ticket:{}", ticket);
            return null;
        }
        log.info("票据校验通过, ticket:{}, username:{}", ticket, t.username);
        return buildUser(t.username);
    }

    /**
     * 销毁全局会话及其名下未使用的票据
     */
    public void destroyGlobalSession(String tgc) {
        GlobalSession session = globalSessions.remove(tgc);
        if (session == null) {
            return;
        }
        tickets.values().removeIf(t -> t.tgc.equals(tgc));
        log.info("销毁全局会话, tgc:{}, username:{}", tgc, session.username);
    }

    private SsoUser buildUser(String username) {
        return new SsoUser(username, NICKNAME_STORE.getOrDefault(username, username));
    }

    /**
     * 全局会话信息
     */
    private static class GlobalSession {
        private final String tgc;
        private final String username;
        private final long createTime;

        GlobalSession(String tgc, String username, long createTime) {
            this.tgc = tgc;
            this.username = username;
            this.createTime = createTime;
        }

        boolean expired() {
            return System.currentTimeMillis() - createTime > SsoConstants.GLOBAL_SESSION_TTL;
        }
    }

    /**
     * 服务票据信息
     */
    private static class Ticket {
        private final String ticket;
        private final String tgc;
        private final String username;
        private final long createTime;

        Ticket(String ticket, String tgc, String username, long createTime) {
            this.ticket = ticket;
            this.tgc = tgc;
            this.username = username;
            this.createTime = createTime;
        }

        boolean expired() {
            return System.currentTimeMillis() - createTime > SsoConstants.TICKET_TTL;
        }
    }
}
