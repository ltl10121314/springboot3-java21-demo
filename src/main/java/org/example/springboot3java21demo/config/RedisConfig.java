package org.example.springboot3java21demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate redisTemplate(@Autowired RedisConnectionFactory redisConnectionFactory) {
        // 创建RedisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 字符串和JDK序列化器
        RedisSerializer<String> strSerializer = RedisSerializer.string();
        RedisSerializer<Object> jdkSerializer = RedisSerializer.java();
        // 设置键值序列化器
        redisTemplate.setKeySerializer(strSerializer);
        redisTemplate.setValueSerializer(jdkSerializer);
        // 设置哈希字段和值序列化器
        redisTemplate.setHashKeySerializer(strSerializer);
        redisTemplate.setHashValueSerializer(jdkSerializer);
        // 给redisTemplate设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}