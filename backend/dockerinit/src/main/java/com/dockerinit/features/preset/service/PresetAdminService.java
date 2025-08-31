package com.dockerinit.features.preset.service;

import com.dockerinit.features.preset.domain.PresetDocument;
import com.dockerinit.features.preset.dto.request.PresetCreateRequest;
import com.dockerinit.features.preset.dto.response.PresetDetailResponse;
import com.dockerinit.features.preset.mapper.PresetMapper;
import com.dockerinit.features.preset.repository.PresetRepository;
import com.dockerinit.global.exception.InvalidInputCustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PresetAdminService {

    private final PresetRepository repository;

    public PresetDetailResponse create(PresetCreateRequest request) {
        String slug = request.slug().trim().toLowerCase(Locale.ROOT);
        if (repository.existsBySlug(slug)) {
            throw new InvalidInputCustomException("slug 가 이미 존재합니다", Map.of("slug", slug));
        }
        // TODO 회원 만들면  admin 의 아이디 혻은 이름 넣기 지금은 GJ 로 하드코딩
        PresetDocument document = PresetMapper.toDocument(request, "GJ");
        PresetDocument created = repository.save(document);
        return PresetMapper.toDetail(created);
    }
}
