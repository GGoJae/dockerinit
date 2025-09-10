package com.dockerinit.features.application.dockercompose.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class ComposeServicePresetAdminService {

    @CacheEvict(cacheNames = "composePreset:list", allEntries = true)
    public void createServicePreset() {
        // TODO 서비스 프리셋 등록하는 로직
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "composePreset:detail", key = "#slug"),
            @CacheEvict(cacheNames = "composePreset:list", allEntries = true)
    })
    public void update(String slug) {
        // TODO 서비스 프리셋 업데이트 하는 로직
    }
}
