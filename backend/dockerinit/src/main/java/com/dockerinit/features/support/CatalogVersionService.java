package com.dockerinit.features.support;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogVersionService {
    private final StringRedisTemplate redis;
    private static final String PRESET_VER_KEY = "di:preset:catalog:ver";
    private static final String COMPOSE_PRESET_VER_KEY = "di:compose:preset:catalog:ver";

    private long getOrZero(String key) {
        String v = redis.opsForValue().get(key);
        return (v == null) ? 0L : Long.parseLong(v);
    }

    private long bump(String key) {
        Long v = redis.opsForValue().increment(key);
        return (v == null) ? 0L : v;
    }

    public long getPresetCatalogVer() {
        return getOrZero(PRESET_VER_KEY);
    }

    public long bumpPresetCatalogVer() {
        return bump(PRESET_VER_KEY);
    }

    public long getComposePresetCatalogVer() {
        return getOrZero(COMPOSE_PRESET_VER_KEY);
    }

    public long bumpComposePresetCatalogVer() {
        return bump(COMPOSE_PRESET_VER_KEY);
    }
}
