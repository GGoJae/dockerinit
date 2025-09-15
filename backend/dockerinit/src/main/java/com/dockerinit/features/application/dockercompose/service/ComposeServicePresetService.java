package com.dockerinit.features.application.dockercompose.service;

import com.dockerinit.features.application.dockercompose.domain.composePreset.Category;
import com.dockerinit.features.application.dockercompose.dto.response.ComposeServicePresetDetailResponse;
import com.dockerinit.features.application.dockercompose.dto.response.ComposeServicePresetSummaryResponse;
import com.dockerinit.features.application.dockercompose.dto.spec.CategoryDTO;
import com.dockerinit.features.application.dockercompose.mapper.ComposeServicePresetMapper;
import com.dockerinit.features.application.dockercompose.repository.ComposeServicePresetRepository;
import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.global.validation.Slug;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ComposeServicePresetService {

    private final ComposeServicePresetRepository repository;

    @Cacheable(cacheNames = "composePreset:list",
            keyGenerator = "composePresetListKeyGen",
            unless = "#pageable.pageSize > 100")
    public Page<ComposeServicePresetSummaryResponse> list(CategoryDTO ctDTO, Set<String> rawTags, Pageable pageable) {
        Set<String> tags = ComposeServicePresetMapper.normalizeTags(rawTags);
        Category category = ComposeServicePresetMapper.dtoToCategory(ctDTO);

        // 나중에 tags  를 모두 포함한 값을 찾고싶으면 matchAll 인자에 스위치 넣을 수 있게 request 객체 수정
        return repository.search(category, tags, false, pageable,true)
                .map(ComposeServicePresetMapper::toSummary);
    }

    @Cacheable(cacheNames = "composePreset:detail", key = "#slug")
    public ComposeServicePresetDetailResponse get(String rawSlug) {
        String slug = Slug.normalizeRequired(rawSlug);
        return repository.findBySlug(slug)
                .map(ComposeServicePresetMapper::toDetail)
                .orElseThrow(() -> NotFoundCustomException.of("composeServicePreset", "slug", rawSlug));
    }

}
