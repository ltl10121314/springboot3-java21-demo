package org.example.practice.web.sso;

/**
 * 认证中心票据校验接口的统一返回
 */
public record ValidateResult(int code, String msg, SsoUser data) {
}
