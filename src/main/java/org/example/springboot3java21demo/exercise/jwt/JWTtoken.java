package org.example.springboot3java21demo.exercise.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class JWTtoken {
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> obj = new HashMap<>();
        obj.put("oid", "1");
        obj.put("userName", "administrator");
        obj.put("tenantId", "tenant_system");
        obj.put("loginId", "");
        obj.put("isAdmin", "");
        obj.put("roles", "");
        String userInfo = null;
        userInfo = Base64.getEncoder().encodeToString(obj.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("userInfo:" + userInfo);
    }
}
