package com.dockerinit.features.dockercompose.service;

import com.dockerinit.features.dockercompose.domain.composePreset.Category;
import com.dockerinit.features.dockercompose.dto.response.ComposeServicePresetDetailResponse;
import com.dockerinit.features.dockercompose.dto.response.ComposeServicePresetSummaryResponse;
import com.dockerinit.features.dockercompose.dto.spec.CategoryDTO;
import com.dockerinit.features.dockercompose.mapper.ComposeServicePresetMapper;
import com.dockerinit.features.dockercompose.repository.ComposeServicePresetRepository;
import com.dockerinit.global.exception.NotFoundCustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ComposeServicePresetService {

    private final ComposeServicePresetRepository repository;

    public Page<ComposeServicePresetSummaryResponse> list(CategoryDTO ctDTO, Set<String> tags, Pageable pageable) {
        // TODO custom repository 에 동적 쿼리 만들어서 한번에 처리하기
        if (tags != null && !tags.isEmpty()) {
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

    public ComposeServicePresetDetailResponse get(String rawSlug) {
        String slug = ComposeServicePresetMapper.normalizeSlug(rawSlug);
        return repository.findBySlug(slug)
                .map(ComposeServicePresetMapper::toDetail)
                .orElseThrow(() -> new NotFoundCustomException("서비스 프리셋을 찾을 수 없습니다.", Map.of("slug", rawSlug)));
    }

    /** 사용 시점에 카운트 올리고 싶으면 호출 */
//    public void markUsed(String rawSlug) {
//        String slug = ComposeServicePresetMapper.normalizeSlug(rawSlug);
//        ComposeServicePresetDocument doc = repository.findBySlug(slug)
//                .orElseThrow(() -> new NotFoundCustomException("서비스 프리셋을 찾을 수 없습니다.", Map.of("slug", rawSlug)));
//        repository.increaseUsedCount(doc.getId(), 1);
//    }
}
