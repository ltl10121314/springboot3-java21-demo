package org.example.springboot3java21demo.common.web;

import jakarta.servlet.http.HttpServletRequest;

public final class WebRequestSupport {

    private WebRequestSupport() {
    }

    public static boolean isInfrastructureRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/webjars/")
                || uri.startsWith("/favicon.ico")
                || uri.startsWith("/error");
    }
}
