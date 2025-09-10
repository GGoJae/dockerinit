package com.dockerinit.features.application.preset.service;

import com.dockerinit.features.application.preset.domain.PresetDocument;
import com.dockerinit.features.application.preset.dto.request.PresetCreateRequest;
import com.dockerinit.features.application.preset.dto.request.PresetUpdateRequest;
import com.dockerinit.features.application.preset.dto.response.PresetDetailResponse;
import com.dockerinit.features.application.preset.mapper.PresetMapper;
import com.dockerinit.features.application.preset.repository.PresetRepository;
import com.dockerinit.features.application.preset.support.PresetSlugFactory;
import com.dockerinit.features.support.validation.Slug;
import com.dockerinit.global.exception.InternalErrorCustomException;
import com.dockerinit.global.exception.NotFoundCustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PresetAdminService {

    private final PresetRepository repository;

    @CacheEvict(cacheNames = {"preset:list", "preset:detail", "preset:artifacts"}, allEntries = true)
    public PresetDetailResponse create(PresetCreateRequest request) {
        final String baseSlug = PresetSlugFactory.build(request.presetKind(), request.slugTokens(), request.schemaVersion());
        String slug = baseSlug;

        // TODO: 인증 붙으면 createdBy 교체
        for (int attempt = 0; attempt < 10; attempt++) {
            try {
                PresetDocument doc = PresetMapper.createDocument(slug, request, "GJ");
                PresetDocument saved = repository.save(doc);
                return PresetMapper.toDetail(saved);
            } catch (org.springframework.dao.DuplicateKeyException | com.mongodb.DuplicateKeyException e) {
                slug = PresetSlugFactory.withNumericSuffix(baseSlug, attempt + 2);
            }
        }
        throw new InternalErrorCustomException("slug 중복이 계쏙 발생합니다. 잠시 후 다시 시도해주세요.");
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "preset:list", allEntries = true),
            @CacheEvict(cacheNames = "preset:detail", key = "T(com.dockerinit.features.support.validation.Slug).normalize(#rawSlug)"),
            @CacheEvict(cacheNames = "preset:artifacts", key = "T(com.dockerinit.features.support.validation.Slug).normalize(#rawSlug)")
    })
    public PresetDetailResponse update(String rawSlug, PresetUpdateRequest request) {
        String slug = Slug.normalize(rawSlug);
        // TODO 회원 만들고 시큐리티 적용시키면 admin 의 아이디 혹은 이름 넣기 지금은 GJ 로 하드코딩
        PresetDocument doc = repository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundCustomException("slug를 찾을 수 없습니다.", Map.of("slug", rawSlug)));
        PresetDocument merged = PresetMapper.merge(doc, request, "GJ");
        return PresetMapper.toDetail(repository.save(merged));
    }

}
