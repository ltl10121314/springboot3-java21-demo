package org.example.springboot3java21demo.common.utils;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseUtils {
    private static final Logger logger = LoggerFactory.getLogger(ResponseUtils.class);

    public ResponseUtils() {
    }

    public static void renderJson(HttpServletResponse response, String json) {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json;charset=utf-8");
        try {
            response.getWriter().write(json);
        } catch (IOException e) {
            logger.error("ResponseUtils.renderJson error", e);
        }

    }
}
