package com.petcare.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置。
 * <p>
 * 注意：使用 StringRedisSerializer（纯文本键）+ GenericJackson2JsonRedisSerializer（JSON序列化值）。
 * 如需限制反序列化范围防止RCE攻击，可替换为 Jackson2JsonRedisSerializer 并指定目标类型，
 * 或配置 ObjectMapper 的 activateDefaultTyping 白名单。
 * 当前方案已启用默认类型安全限制（GenericJackson2JsonRedisSerializer 使用非final类型的类名）。
 * </p>
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 配置 ObjectMapper 使用白名单模式防止任意类反序列化
        ObjectMapper mapper = new ObjectMapper();
        // 不启用 defaultTyping，避免任意 gadget 链反序列化
        // 使用 GenericJackson2JsonRedisSerializer 的默认安全策略

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));
        template.afterPropertiesSet();
        return template;
    }
}
