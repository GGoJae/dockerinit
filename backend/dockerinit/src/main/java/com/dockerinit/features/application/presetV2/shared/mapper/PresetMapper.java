package com.dockerinit.features.application.presetV2.shared.mapper;

import com.dockerinit.features.application.presetV2.compose.domain.ComposePresetDocument;
import com.dockerinit.features.application.presetV2.composeService.domain.ComposeServicePresetDocument;
import com.dockerinit.features.application.presetV2.dockerfile.domain.DockerfilePresetDocument;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetDetailResponse;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetSummaryResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PresetMapper {

    private PresetMapper() {}

    private static long nz(Long v) { return v == null ? 0L : v; }
    private static int sizeOf(List<?> l) { return l == null ? 0 : l.size(); }

    // ───────────── Dockerfile ─────────────
    public static PresetSummaryResponse toSummary(DockerfilePresetDocument d) {
        var m = Objects.requireNonNull(d.getMeta(), "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();
        return new PresetSummaryResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                d.getUpdatedAt(),
                d.getVersion(), // 문서의 @Version 사용
                nz(metrics.getViewed()),
                nz(metrics.getApplied()),
                nz(metrics.getCopied()),
                nz(metrics.getDownloaded())
        );
    }

    public static PresetDetailResponse toDetail(DockerfilePresetDocument d) {
        Map<String, Object> payloadMeta = new LinkedHashMap<>();
        boolean hasHealth = d.getPlan() != null && d.getPlan().healthcheck() != null;
        int exposeCount = (d.getPlan() == null) ? 0 : sizeOf(d.getPlan().expose());

        payloadMeta.put("type", "DockerfilePlan");
        payloadMeta.put("hasHealthcheck", hasHealth);
        payloadMeta.put("exposeCount", exposeCount);

        var m = Objects.requireNonNull(d.getMeta(), "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();

        return new PresetDetailResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                d.getUpdatedAt(),
                d.getVersion(),
                payloadMeta,
                nz(metrics.getViewed()),
                nz(metrics.getApplied()),
                nz(metrics.getCopied()),
                nz(metrics.getDownloaded())
        );
    }

    // ───────────── Compose(full) ─────────────
    public static PresetSummaryResponse toSummary(ComposePresetDocument d) {
        var m = Objects.requireNonNull(d.getMeta(), "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();
        return new PresetSummaryResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                d.getUpdatedAt(),
                d.getVersion(),
                nz(metrics.getViewed()),
                nz(metrics.getApplied()),
                nz(metrics.getCopied()),
                nz(metrics.getDownloaded())
        );
    }

    public static PresetDetailResponse toDetail(ComposePresetDocument d) {
        Map<String, Object> payloadMeta = new LinkedHashMap<>();
        int svcCount = (d.getPlan() == null) ? 0 : sizeOf(d.getPlan().services());

        payloadMeta.put("type", "ComposePlan");
        payloadMeta.put("serviceCount", svcCount);

        var m = Objects.requireNonNull(d.getMeta(), "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();

        return new PresetDetailResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                d.getUpdatedAt(),
                d.getVersion(),
                payloadMeta,
                nz(metrics.getViewed()),
                nz(metrics.getApplied()),
                nz(metrics.getCopied()),
                nz(metrics.getDownloaded())
        );
    }

    // ───────────── Compose(Service piece) ─────────────
    public static PresetSummaryResponse toSummary(ComposeServicePresetDocument d) {
        var m = Objects.requireNonNull(d.getMeta(), "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();
        return new PresetSummaryResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                d.getUpdatedAt(),
                d.getVersion(),
                nz(metrics.getViewed()),
                nz(metrics.getApplied()),
                nz(metrics.getCopied()),
                nz(metrics.getDownloaded())
        );
    }

    public static PresetDetailResponse toDetail(ComposeServicePresetDocument d) {
        Map<String, Object> payloadMeta = new LinkedHashMap<>();
        payloadMeta.put("type", "ComposeService");

        var m = Objects.requireNonNull(d.getMeta(), "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();

        return new PresetDetailResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                d.getUpdatedAt(),
                d.getVersion(),
                payloadMeta,
                nz(metrics.getViewed()),
                nz(metrics.getApplied()),
                nz(metrics.getCopied()),
                nz(metrics.getDownloaded())
        );
    }
}
