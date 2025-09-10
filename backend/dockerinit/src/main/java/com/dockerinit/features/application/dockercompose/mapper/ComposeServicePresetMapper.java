package com.dockerinit.features.application.dockercompose.mapper;

import com.dockerinit.features.application.dockercompose.domain.Service;
import com.dockerinit.features.application.dockercompose.domain.composePreset.Category;
import com.dockerinit.features.application.dockercompose.domain.composePreset.ComposeServicePresetDocument;
import com.dockerinit.features.application.dockercompose.dto.response.ComposeServicePresetDetailResponse;
import com.dockerinit.features.application.dockercompose.dto.response.ComposeServicePresetSummaryResponse;
import com.dockerinit.features.application.dockercompose.dto.spec.BuildDTO;
import com.dockerinit.features.application.dockercompose.dto.spec.CategoryDTO;
import com.dockerinit.features.application.dockercompose.dto.spec.HealthcheckDTO;
import com.dockerinit.features.application.dockercompose.dto.spec.ServiceSpecDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComposeServicePresetMapper {

    public static ComposeServicePresetSummaryResponse toSummary(ComposeServicePresetDocument doc) {
        return new ComposeServicePresetSummaryResponse(
                doc.getId(),
                doc.getSlug(),
                doc.getDisplayName(),
                toDTO(doc.getCategory()),
                doc.getTags(),
                doc.getUpdatedAt(),
                doc.getUsedCount()
        );
    }

    public static ComposeServicePresetDetailResponse toDetail(ComposeServicePresetDocument doc) {
        return new ComposeServicePresetDetailResponse(
                doc.getId(),
                doc.getSlug(),
                doc.getDisplayName(),
                doc.getDescription(),
                toDTO(doc.getCategory()),
                doc.getTags(),
                doc.getSchemaVersion(),
                toServiceDTO(doc.getService()),
                doc.getSuggestedEnvDefaults(),
                doc.getActive(),
                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                doc.getUsedCount()
        );
    }


    public static CategoryDTO toDTO(Category category) {
        if (category == null) return CategoryDTO.UNKNOWN;
        return switch (category) {
            case LANGUAGE -> CategoryDTO.LANGUAGE;
            case DATABASE -> CategoryDTO.DATABASE;
            case CACHE -> CategoryDTO.CACHE;
            case QUEUE -> CategoryDTO.QUEUE;
            case OBSERVABILITY -> CategoryDTO.OBSERVABILITY;
            case RUNTIME -> CategoryDTO.RUNTIME;
            case FRAMEWORK -> CategoryDTO.FRAMEWORK;
            case OTHER -> CategoryDTO.OTHER;
        };
    }

    public static Category toDomain(CategoryDTO dto) {
        if (dto == null) return Category.OTHER;
        return switch (dto) {
            case LANGUAGE -> Category.LANGUAGE;
            case DATABASE -> Category.DATABASE;
            case CACHE -> Category.CACHE;
            case QUEUE -> Category.QUEUE;
            case OBSERVABILITY -> Category.OBSERVABILITY;
            case RUNTIME -> Category.RUNTIME;
            case FRAMEWORK -> Category.FRAMEWORK;
            case OTHER, UNKNOWN -> Category.OTHER;
        };
    }

    public static ServiceSpecDTO toServiceDTO(Service s) {
        if (s == null) return null;
        return new ServiceSpecDTO(
                s.name(),
                s.image(),
                s.build() == null ? null :
                        new BuildDTO(
                                s.build().context(),
                                s.build().dockerfile(),
                                s.build().args()
                        ),
                s.command(),
                s.environment(),
                s.envFile(),
                s.ports(),
                s.volumes(),
                s.dependsOn(),
                s.restart(),
                s.healthcheck() == null ? null :
                        new HealthcheckDTO(
                                s.healthcheck().test(),
                                s.healthcheck().interval(),
                                s.healthcheck().timeout(),
                                s.healthcheck().retries(),
                                s.healthcheck().startPeriod()
                        )
        );
    }

    /* slug 정규화 유틸 */
    public static String normalizeSlug(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }

    /* 태그 정규화(소문자/trim) */
    public static Set<String> normalizeTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) return Set.of();
        return tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(t -> t.trim().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    /* env 기본값 정규화(널 방지) */
    public static Map<String,String> safeEnvDefaults(Map<String,String> m) {
        return (m == null || m.isEmpty()) ? Map.of() : Map.copyOf(m);
    }
}
