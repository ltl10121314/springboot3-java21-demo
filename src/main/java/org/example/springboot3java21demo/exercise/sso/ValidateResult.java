package org.example.springboot3java21demo.exercise.sso;

/**
 * 认证中心票据校验接口的统一返回
 */
public record ValidateResult(int code, String msg, SsoUser data) {
}
