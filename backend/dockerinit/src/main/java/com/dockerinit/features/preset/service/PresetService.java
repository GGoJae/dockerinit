package com.dockerinit.features.preset.service;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.PackageResult;
import com.dockerinit.features.packager.Packager;
import com.dockerinit.features.preset.domain.PresetDocument;
import com.dockerinit.features.preset.domain.PresetKind;
import com.dockerinit.features.preset.dto.response.PresetArtifactMetaResponse;
import com.dockerinit.features.preset.dto.response.PresetDetailResponse;
import com.dockerinit.features.preset.dto.response.PresetSummaryResponse;
import com.dockerinit.features.preset.dto.spec.PresetKindDTO;
import com.dockerinit.features.preset.mapper.PresetMapper;
import com.dockerinit.features.preset.repository.PresetRepository;
import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.exception.NotFoundCustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PresetService {

    private final PresetRepository repository;
    private final Packager packager;

    public Page<PresetSummaryResponse> list(PresetKindDTO dto, Set<String> tags, Pageable pageable) {
        // TODO 커스텀 으로 동적 쿼리 만들기
        if (tags != null && !tags.isEmpty()) {
            return repository.findAllByTagsInAndActiveTrue(tags, pageable)
                    .map(PresetMapper::toSummary);
        }
        if (dto != null) {
            PresetKind kind = PresetMapper.toDomain(dto);
            return repository.findAllByPresetKindAndActiveTrue(kind, pageable)
                    .map(PresetMapper::toSummary);
        }
        return repository
                .findAllByActiveTrue(pageable)
                .map(PresetMapper::toSummary);
    }

    public PresetDetailResponse get(String rawSlug) {
        String slug = rawSlug.trim().toLowerCase(Locale.ROOT);
        return repository.findBySlug(slug)
                .map(PresetMapper::toDetail)
                .orElseThrow(() -> new NotFoundCustomException("해당 preset을 찾을 수 없습니다.", Map.of("slug", rawSlug)));
    }

    public List<PresetArtifactMetaResponse> artifacts(String rawSlug) {
        String slug = rawSlug.trim().toLowerCase(Locale.ROOT);
        return repository.findBySlug(slug)
                .map(PresetDocument::getArtifacts)
                .map(PresetMapper::mapArtifactsToRes)
                .orElseThrow(() -> new NotFoundCustomException("해당 preset을 찾을 수 없습니다.", Map.of("slug", rawSlug)));
    }

    public PackageResult packagePreset(String rawSlug, Set<FileType> rawTargets) {
        String slug = rawSlug.trim().toLowerCase(Locale.ROOT);
        PresetDocument document = repository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundCustomException("해당 preset을 찾을 수 없습니다.", Map.of("slug", rawSlug)));

        Set<FileType> targets = (rawTargets == null || rawTargets.isEmpty()) ? document.getDefaultTargets() : rawTargets;

        String packageName = "%s-v%d".formatted(document.getSlug(), Optional.ofNullable(document.getSchemaVersion()).orElse(1));
        List<GeneratedFile> files = PresetMapper.toGeneratedFiles(document.getArtifacts(), targets);

        if (files.isEmpty()) {
            throw new InvalidInputCustomException(
                    "선택한 targets에 해당하는 파일이 없습니다.",
                    Map.of("slug", rawSlug, "targets", String.valueOf(targets))
            );
        }
        return packager.packageFiles(files, packageName);
    }


}
