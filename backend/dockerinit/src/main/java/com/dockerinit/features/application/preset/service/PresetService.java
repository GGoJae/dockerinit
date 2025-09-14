package com.dockerinit.features.application.preset.service;

import com.dockerinit.features.application.preset.domain.PresetArtifact;
import com.dockerinit.features.application.preset.domain.PresetDocument;
import com.dockerinit.features.application.preset.domain.PresetKind;
import com.dockerinit.features.application.preset.dto.response.PresetArtifactResponse;
import com.dockerinit.features.application.preset.dto.response.PresetDetailResponse;
import com.dockerinit.features.application.preset.dto.response.PresetSummaryResponse;
import com.dockerinit.features.application.preset.dto.spec.PresetKindDTO;
import com.dockerinit.features.application.preset.mapper.PresetMapper;
import com.dockerinit.features.application.preset.materializer.PresetArtifactMaterializer;
import com.dockerinit.features.application.preset.repository.PresetRepository;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.PackageResult;
import com.dockerinit.features.packager.Packager;
import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.global.validation.Slug;
import com.dockerinit.global.validation.ValidationCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresetService {

    private final PresetRepository repository;
    private final Packager packager;
    private final PresetArtifactMaterializer materializer;

    @Cacheable(
            cacheNames = "preset:list",
            keyGenerator = "presetListKeyGen"
    )
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

    @Cacheable(
            cacheNames = "preset:detail",
            key = "T(com.dockerinit.features.support.validation.Slug).normalize(#rawSlug)"
    )
    public PresetDetailResponse get(String rawSlug) {
        String slug = Slug.normalizeRequired(rawSlug);
        return repository.findBySlug(slug)
                .map(PresetMapper::toDetail)
                .orElseThrow(() -> NotFoundCustomException.of("Preset", "slug", rawSlug));
    }

    @Cacheable(
            cacheNames = "preset:artifacts",
            key = "T(com.dockerinit.features.support.validation.Slug).normalize(#rawSlug)"
    )
    public PresetArtifactResponse artifacts(String rawSlug) {
        String slug = Slug.normalizeRequired(rawSlug);
        return repository.findBySlug(slug)
                .map(PresetMapper::toArtifactResponse)
                .orElseThrow(() -> NotFoundCustomException.of("PresetArtifacts", "slug", rawSlug));
    }

    public PackageResult packagePreset(String rawSlug, Set<FileType> rawTargets) {
        String slug = Slug.normalizeRequired(rawSlug);
        PresetDocument document = repository.findBySlug(slug)
                .orElseThrow(() -> NotFoundCustomException.of("Preset", "slug", rawSlug));

        Set<FileType> targets = (rawTargets == null || rawTargets.isEmpty())
                ? (document.getDefaultTargets().isEmpty()
                    ? document.getArtifacts().stream().map(PresetArtifact::getFileType).collect(Collectors.toSet())
                    : document.getDefaultTargets())
                : rawTargets;

        String packageName = "%s-v%d".formatted(document.getSlug(), Optional.ofNullable(document.getSchemaVersion()).orElse(1));
        List<GeneratedFile> files = materializer.toGeneratedFiles(document.getArtifacts(), targets);

        ValidationCollector.create()
                .deferThrowIf(files.isEmpty())
                .topMessage("선택한 Preset 안에 targets에 해당하는 파일이 없습니다.")
                .withField("slug", rawSlug)
                .withField("targets", targets)
                .throwIfInvalid();

        PackageResult result = packager.packageFiles(files, packageName);
        repository.increaseDownloadCount(document.getId(), 1);
        return result;
    }


}
