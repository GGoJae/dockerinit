package com.dockerinit.features.application.presetV2.shared.service;

import com.dockerinit.features.application.presetV2.compose.domain.ComposePresetDocument;
import com.dockerinit.features.application.presetV2.compose.repository.ComposePresetRepository;
import com.dockerinit.features.application.presetV2.composeService.domain.ComposeServicePresetDocument;
import com.dockerinit.features.application.presetV2.composeService.repository.ComposeServicePresetRepositoryV2;
import com.dockerinit.features.application.presetV2.dockerfile.domain.DockerfilePresetDocument;
import com.dockerinit.features.application.presetV2.dockerfile.repository.DockerfilePresetRepository;
import com.dockerinit.features.application.presetV2.shared.domain.PresetKind;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetDetailResponse;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetSummaryResponse;
import com.dockerinit.features.application.presetV2.shared.mapper.PresetMapper;
import com.dockerinit.global.exception.IllegalArgumentCustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PresetQueryService {

    private final DockerfilePresetRepository dockerfileRepo;
    private final ComposePresetRepository composeRepo;
    private final ComposeServicePresetRepositoryV2 composeServiceRepo;

    public Page<PresetSummaryResponse> list(PresetKind kind, Set<String> tags, Pageable pageable) {
        return switch (kind) {
            case DOCKERFILE -> {
                Page<DockerfilePresetDocument> p = (tags == null || tags.isEmpty())
                        ? dockerfileRepo.findByMeta_ActiveTrue(pageable)
                        : dockerfileRepo.findByMeta_ActiveTrueAndMeta_TagsIn(tags, pageable);
                yield p.map(PresetMapper::toSummary);
            }
            case COMPOSE -> {
                Page<ComposePresetDocument> p = (tags == null || tags.isEmpty())
                        ? composeRepo.findByMeta_ActiveTrue(pageable)
                        : composeRepo.findByMeta_ActiveTrueAndMeta_TagsIn(tags, pageable);
                yield p.map(PresetMapper::toSummary);
            }
            case COMPOSE_SERVICE -> {
                Page<ComposeServicePresetDocument> p = (tags == null || tags.isEmpty())
                        ? composeServiceRepo.findByMeta_ActiveTrue(pageable)
                        : composeServiceRepo.findByMeta_ActiveTrueAndMeta_TagsIn(tags, pageable);
                yield p.map(PresetMapper::toSummary);
            }
        };
    }

    public PresetDetailResponse get(PresetKind kind, String slug) {
        return switch (kind) {
            case DOCKERFILE -> dockerfileRepo.findByMeta_Slug(slug)
                    .map(PresetMapper::toDetail)
                    .orElseThrow(() -> new IllegalArgumentCustomException("dockerfile preset not found: " + slug));
            case COMPOSE -> composeRepo.findByMeta_Slug(slug)
                    .map(PresetMapper::toDetail)
                    .orElseThrow(() -> new IllegalArgumentCustomException("compose preset not found: " + slug));
            case COMPOSE_SERVICE -> composeServiceRepo.findByMeta_Slug(slug)
                    .map(PresetMapper::toDetail)
                    .orElseThrow(() -> new IllegalArgumentCustomException("compose-service preset not found: " + slug));
        };
    }
}
