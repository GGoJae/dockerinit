package com.dockerinit.features.application.dockercompose.service;

import com.dockerinit.features.application.dockercompose.dto.admin.ComposeServicePresetCreateRequest;
import com.dockerinit.features.application.dockercompose.dto.admin.ComposeServicePresetUpdateRequest;
import com.dockerinit.features.application.dockercompose.repository.ComposeServicePresetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComposeServicePresetAdminService {

    private final ComposeServicePresetRepository repository;

    @CacheEvict(cacheNames = "composePreset:list", allEntries = true)
    public void create(ComposeServicePresetCreateRequest request) {
        // TODO 서비스 프리셋 등록하는 로직
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "composePreset:detail", key = "#slug"),
            @CacheEvict(cacheNames = "composePreset:list", allEntries = true)
    })
    public void update(String slug, ComposeServicePresetUpdateRequest request) {
        // TODO 서비스 프리셋 업데이트 하는 로직
    }

    public void setActive(String slug, boolean b) {
    }

    public void delete(String slug) {

    }
}
