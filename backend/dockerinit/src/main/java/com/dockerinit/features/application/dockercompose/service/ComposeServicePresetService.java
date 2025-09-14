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
        // TODO custom repository 에 동적 쿼리 만들어서 한번에 처리하기
        if (rawTags != null && !rawTags.isEmpty()) {
            return repository.findAllByTagsInAndActiveTrue(tags, pageable)
                    .map(ComposeServicePresetMapper::toSummary);
        }
        if (ctDTO != null) {
            Category category = ComposeServicePresetMapper.toDomain(ctDTO);
            return repository.findAllByCategoryAndActiveTrue(category, pageable)
                    .map(ComposeServicePresetMapper::toSummary);
        }

        return repository.findAllByActiveTrue(pageable)
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
