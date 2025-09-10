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

    public String getPresetCatalogVer() {
        String v = redis.opsForValue().get(PRESET_VER_KEY);
        return (v == null) ? "0" : v;
    }

    public void bumpPresetCatalogVer() {
        redis.opsForValue().increment(PRESET_VER_KEY);
    }

    public String getComposePresetCatalogVer() {
        String v = redis.opsForValue().get(COMPOSE_PRESET_VER_KEY);
        return (v == null) ? "0" : v;
    }

    public void bumpComposePresetCatalogVer() {
        redis.opsForValue().increment(COMPOSE_PRESET_VER_KEY);
    }
}
