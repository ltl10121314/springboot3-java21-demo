package org.example.practice.web.sso;

/**
 * SSO 用户信息，登录成功后由认证中心下发给各业务系统
 */
public record SsoUser(String username, String nickname) {
}
