package com.dockerinit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        StringRedisTemplate t = new StringRedisTemplate();
        t.setConnectionFactory(cf);
        StringRedisSerializer s = new StringRedisSerializer();
        t.setKeySerializer(s);
        t.setValueSerializer(s);
        t.setHashKeySerializer(s);
        t.setHashValueSerializer(s);
        t.afterPropertiesSet();
        return t;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory cf, ObjectMapper om) {
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(om);
        RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .prefixCacheNameWith("di:cache:")
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                )
                .entryTtl(Duration.ofMinutes(10));

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put("preset:list",    cfg.entryTtl(Duration.ofSeconds(60)));
        perCache.put("preset:detail",  cfg.entryTtl(Duration.ofMinutes(10)));
        perCache.put("preset:artifacts", cfg.entryTtl(Duration.ofMinutes(10)));

        perCache.put("composePreset:detail", cfg.entryTtl(Duration.ofMinutes(10)));
        perCache.put("composePreset:list",   cfg.entryTtl(Duration.ofSeconds(60)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(cfg)
                .withInitialCacheConfigurations(perCache)
                .build();

    }

}
