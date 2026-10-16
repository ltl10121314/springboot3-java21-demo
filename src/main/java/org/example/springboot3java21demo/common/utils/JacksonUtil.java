package org.example.springboot3java21demo.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Jackson JSON 工具类
 * <p>
 * 支持：
 * 1. Java 对象转 JSON
 * 2. JSON 转 Java 对象
 * 3. JSON 转 List
 * 4. JSON 转 Map
 * 5. JSON 转 JsonNode
 */
public final class JacksonUtil {

    /**
     * 全局 ObjectMapper。
     * 完成配置后复用，不要在每次调用时重新创建。
     */
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            // 忽略 JSON 中目标对象没有的字段
            .configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false
            )
            // 反序列化时，空字符串不自动转换为 null 的相关规则保持默认
            // 输出 JSON 时，不强制缩进格式
            .disable(SerializationFeature.INDENT_OUTPUT)
            .build();

    private JacksonUtil() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated"
        );
    }

    /**
     * Java 对象转 JSON 字符串
     */
    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Java 对象转 JSON 失败", e
            );
        }
    }

    /**
     * JSON 字符串转指定类型对象
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        if (clazz == null) {
            throw new IllegalArgumentException(
                    "目标类型 clazz 不能为空"
            );
        }

        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "JSON 转对象失败，目标类型：" + clazz.getName(), e
            );
        }
    }

    /**
     * JSON 字符串转复杂泛型对象
     */
    public static <T> T toObject(
            String json, TypeReference<T> typeReference) {

        if (json == null || json.isBlank()) {
            return null;
        }
        if (typeReference == null) {
            throw new IllegalArgumentException(
                    "typeReference 不能为空"
            );
        }

        try {
            return MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "JSON 转泛型对象失败", e
            );
        }
    }

    /**
     * JSON 数组转 List
     */
    public static <T> List<T> toList(
            String json, Class<T> elementClass) {

        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        if (elementClass == null) {
            throw new IllegalArgumentException(
                    "elementClass 不能为空"
            );
        }

        try {
            return MAPPER.readValue(
                    json,
                    MAPPER.getTypeFactory()
                            .constructCollectionType(
                                    List.class, elementClass
                            )
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "JSON 转 List 失败", e
            );
        }
    }

    /**
     * JSON 转 Map
     */
    public static Map<String, Object> toMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return MAPPER.readValue(
                    json,
                    MAPPER.getTypeFactory()
                            .constructMapType(
                                    Map.class,
                                    String.class,
                                    Object.class
                            )
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "JSON 转 Map 失败", e
            );
        }
    }

    /**
     * JSON 转 JsonNode
     * 适合 JSON 字段不固定的场景
     */
    public static JsonNode toJsonNode(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "JSON 转 JsonNode 失败", e
            );
        }
    }

    /**
     * 判断字符串是否为合法 JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }

        try {
            MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 获取全局 ObjectMapper。
     * 仅在需要高级 Jackson 配置时使用。
     * 获取后不要修改全局配置。
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}

