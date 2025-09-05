package com.dockerinit.features.preset.mapper;

import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.preset.domain.*;
import com.dockerinit.features.preset.dto.request.PresetArtifactRequest;
import com.dockerinit.features.preset.dto.request.PresetCreateRequest;
import com.dockerinit.features.preset.dto.request.PresetUpdateRequest;
import com.dockerinit.features.preset.dto.response.PresetArtifactMetaResponse;
import com.dockerinit.features.preset.dto.response.PresetDetailResponse;
import com.dockerinit.features.preset.dto.response.PresetSummaryResponse;
import com.dockerinit.features.preset.dto.spec.ContentStrategyDTO;
import com.dockerinit.features.preset.dto.spec.EnvValueModeDTO;
import com.dockerinit.features.preset.dto.spec.PresetKindDTO;
import com.dockerinit.features.preset.dto.spec.RenderPolicyDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PresetMapper {

    public static PresetDocument createDocument(String slug, PresetCreateRequest req, String createdBy) {
        String display = req.displayName().trim();

        return PresetDocument.builder()
                .slug(slug)
                .displayName(display)
                .description(trimOrNull(req.description()))
                .presetKind(toDomain(req.presetKind()))
                .tags(safeSet(req.tags()))
                .schemaVersion(req.schemaVersion())
                .renderPolicy(toDomain(req.renderPolicy()))
                .artifacts(mapArtifactsToDomain(req.artifacts()))
                .defaultTargets(safeFileTypes(req.defaultTargets()))
                .instructions(trimOrNull(req.instructions()))
                .active(Boolean.TRUE.equals(req.active()))
                .deprecated(Boolean.TRUE.equals(req.deprecated()))
                .deprecationNote(trimOrNull(req.deprecationNote()))
                .createdBy(createdBy)
                .updatedBy(createdBy)
                // createdAt/updatedAt/version/downloadCount 는 Mongo Auditing/업무 로직이 채움
                .build();
    }

    public static PresetDetailResponse toDetail(PresetDocument doc) {
        PresetKindDTO kindDTO = toDTO(doc.getPresetKind());
        RenderPolicyDTO renderPolicyDTO = toDTO(doc.getRenderPolicy());
        List<PresetArtifactMetaResponse> presetArtifactMetaResponses = mapArtifactsToRes(doc.getArtifacts());

        return new PresetDetailResponse(doc.getId(), doc.getSlug(), doc.getDisplayName(), doc.getDescription(), kindDTO, doc.getTags(),
                doc.getSchemaVersion(), renderPolicyDTO, doc.getDefaultTargets(), doc.getInstructions(), doc.getActive(),
                doc.getDeprecated(), doc.getDeprecationNote(), doc.getCreatedAt(), doc.getUpdatedAt(), doc.getDownloadCount(),
                presetArtifactMetaResponses);

    }

    public static PresetSummaryResponse toSummary(PresetDocument doc) {
        PresetKindDTO kindDTO = toDTO(doc.getPresetKind());

        return new PresetSummaryResponse(
                doc.getId(), doc.getSlug(), doc.getDisplayName(), kindDTO, doc.getTags(), doc.getDeprecated(),
                doc.getDownloadCount(), doc.getUpdatedAt()
        );
    }

    /* ---------- UPDATE (PATCH) ---------- */
    // TODO 지금은 updatedBy GJ 로 하드코딩 시큐리티 도입하면 바꾸기
    public static PresetDocument merge(PresetDocument base, PresetUpdateRequest req, String updatedBy) {
        PresetDocument.PresetDocumentBuilder b = base.toBuilder();

        if (req.displayName() != null) b.displayName(req.displayName().trim());
        if (req.description() != null) b.description(trimOrNull(req.description()));
        if (req.presetKind() != null) b.presetKind(toDomain(req.presetKind()));
        if (req.tags() != null) b.tags(safeSet(req.tags()));
        if (req.schemaVersion() != null) b.schemaVersion(req.schemaVersion());
        if (req.renderPolicy() != null) b.renderPolicy(toDomain(req.renderPolicy()));
        if (req.artifacts() != null) b.artifacts(mapArtifactsToDomain(req.artifacts()));
        if (req.defaultTargets() != null) b.defaultTargets(safeFileTypes(req.defaultTargets()));
        if (req.instructions() != null) b.instructions(trimOrNull(req.instructions()));
        if (req.active() != null) b.active(req.active());
        if (req.deprecated() != null) b.deprecated(req.deprecated());
        if (req.deprecationNote() != null) b.deprecationNote(trimOrNull(req.deprecationNote()));

        b.updatedBy(updatedBy);
        return b.build(); // @LastModifiedDate 는 Auditing이 채움
    }

    /* ---------- helpers ---------- */

    public static List<PresetArtifact> mapArtifactsToDomain(List<PresetArtifactRequest> in) {
        if (in == null || in.isEmpty()) return List.of();
        List<PresetArtifact> list = in.stream().map(r -> mapArtifactToDomain(r)).collect(Collectors.toUnmodifiableList());

        // 중복 파일명 방지: (fileType, filename) 기준
        var keySet = new HashSet<String>();
        for (PresetArtifact a : list) {
            String key = a.getFileType() + "::" + a.getFilename();
            if (!keySet.add(key)) {
                throw new IllegalArgumentException("duplicate artifact: " + key);
            }
        }
        return List.copyOf(list);
    }

    public static PresetArtifact mapArtifactToDomain(PresetArtifactRequest a) {
        ContentStrategy strategy = toDomain(a.strategy());
        // 전략별 필드 유효성
        if (strategy == ContentStrategy.EMBEDDED) {
            if (isBlank(a.inlineContent()))
                throw new IllegalArgumentException("EMBEDDED requires inlineContent");
        } else if (strategy == ContentStrategy.OBJECT_STORAGE) {
            if (isBlank(a.storageKey()))
                throw new IllegalArgumentException("OBJECT_STORAGE requires storageKey");
        }

        return PresetArtifact.builder()
                .fileType(Objects.requireNonNull(a.fileType(), "fileType"))
                .filename(Objects.requireNonNull(a.filename(), "filename"))
                .contentType(toContentType(a.contentType()))
                .strategy(strategy)
                .embeddedContent(a.inlineContent())
                .storageProvider(trimOrNull(a.storageProvider()))
                .storageKey(trimOrNull(a.storageKey()))
                .sensitive(Boolean.TRUE.equals(a.sensitive()))
                .etag(trimOrNull(a.etag()))
                .contentLength(a.contentLength())
                .build();
    }

    public static PresetArtifactMetaResponse mapArtifactToRes(PresetArtifact a) {
        ContentStrategyDTO strategy = toDTO(a.getStrategy());
        String contentTypeToString = contentTypeToString(a.getContentType());
        String normalizeEtag = normalizeEtag(a.getEtag());

        return new PresetArtifactMetaResponse(a.getFileType(), a.getFilename(), contentTypeToString, strategy,
                a.getSensitive(), normalizeEtag, a.getContentLength(), a.getStorageProvider(), a.getStorageKey());
    }

    public static List<PresetArtifactMetaResponse> mapArtifactsToRes(List<PresetArtifact> in) {
        if (in == null || in.isEmpty()) return List.of();
        return in.stream().map(PresetMapper::mapArtifactToRes).toList();
    }

    public static RenderPolicy toDomain(RenderPolicyDTO dto) {
        if (dto == null) return null;
        return RenderPolicy.builder()
                .envValueMode(toDomain(dto.envValueMode()))
                .placeholderFormat(trimOrNull(dto.placeholderFormat()))
                .includeManifestByDefault(Boolean.TRUE.equals(dto.includeManifestByDefault()))
                .ensureTrailingNewline(Boolean.TRUE.equals(dto.ensureTrailingNewline()))
                .lineEndings(trimOrNull(dto.lineEndings()))
                .build();
    }

    public static RenderPolicyDTO toDTO(RenderPolicy rp) {
        if (rp == null) return null;
        EnvValueModeDTO evm = toDTO(rp.getEnvValueMode());
        return new RenderPolicyDTO(evm, rp.getPlaceholderFormat(), rp.getIncludeManifestByDefault(), rp.getEnsureTrailingNewline(),
                rp.getLineEndings());
    }

    public static PresetKind toDomain(PresetKindDTO k) {
        if (k == null) return null;
        return switch (k) {
            case DOCKERFILE -> PresetKind.DOCKERFILE;
            case COMPOSE   -> PresetKind.COMPOSE;
            case BUNDLE    -> PresetKind.BUNDLE;
            case UNKNOWN   -> PresetKind.DOCKERFILE; // 정책 선택
        };
    }

    public static PresetKindDTO toDTO(PresetKind k) {
        if (k == null) return null;
        return switch (k) {
            case DOCKERFILE -> PresetKindDTO.DOCKERFILE;
            case COMPOSE   -> PresetKindDTO.COMPOSE;
            case BUNDLE    -> PresetKindDTO.BUNDLE;

        };
    }

    public static EnvValueModeDTO toDTO(EnvValueMode evm) {
        if (evm == null) return null;
        return switch (evm) {
            case INLINE -> EnvValueModeDTO.INLINE;
            case PLACEHOLDER -> EnvValueModeDTO.PLACEHOLDER;
        };
    }

    public static EnvValueMode toDomain(EnvValueModeDTO m) {
        if (m == null) return null;
        return switch (m) {
            case INLINE -> EnvValueMode.INLINE;
            case PLACEHOLDER -> EnvValueMode.PLACEHOLDER;
        };
    }

    public static ContentStrategy toDomain(ContentStrategyDTO d) {
        if (d == null) return null;
        return switch (d) {
            case EMBEDDED -> ContentStrategy.EMBEDDED;
            case OBJECT_STORAGE -> ContentStrategy.OBJECT_STORAGE;
        };
    }

    public static ContentStrategyDTO toDTO(ContentStrategy d) {
        if (d == null) return null;
        return switch (d) {
            case EMBEDDED -> ContentStrategyDTO.EMBEDDED;
            case OBJECT_STORAGE -> ContentStrategyDTO.OBJECT_STORAGE;
        };
    }

    public static ContentType toContentType(String mime) {
        if (mime == null || mime.isBlank()) return ContentType.OCTET;
        String m = mime.toLowerCase(Locale.ROOT);
        if (m.contains("yaml") || m.contains("yml")) return ContentType.YAML;
        if (m.contains("json")) return ContentType.JSON;
        if (m.contains("markdown") || m.contains("md")) return ContentType.MD;
        if (m.contains("zip")) return ContentType.ZIP;
        if (m.startsWith("text/")) return ContentType.TEXT;
        return ContentType.OCTET;
    }

    public static String contentTypeToString(ContentType type) {
        if (type == null ) return null;
        return type.value();
    }

    public static String normalizeEtag(String etag) {
        if (etag == null || etag.isBlank()) return null;
        String e = etag.trim();
        if (e.startsWith("\"") && e.endsWith("\"") && e.length() > 1) {
            e = e.substring(1, e.length()-1);
        }
        return e;
    }


    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Set<String> safeSet(Set<String> s) {
        if (s == null || s.isEmpty()) return Set.of();
        if (s.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("tags contain null");
        return s.stream()
                .map(t -> t.trim().toLowerCase(Locale.ROOT))
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<FileType> safeFileTypes(Set<FileType> s) {
        if (s == null || s.isEmpty()) return Set.of();
        if (s.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("defaultTargets contain null");
        return Set.copyOf(s);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
