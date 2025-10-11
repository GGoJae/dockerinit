package com.dockerinit.features.application.presetv2.shared.mapper;

import com.dockerinit.features.application.presetv2.compose.domain.ComposePresetDocument;
import com.dockerinit.features.application.presetv2.composeService.domain.ComposeServicePresetDocument;
import com.dockerinit.features.application.presetv2.dockerfile.domain.DockerfilePresetDocument;
import com.dockerinit.features.application.presetv2.shared.dto.response.PresetDetailResponse;
import com.dockerinit.features.application.presetv2.shared.dto.response.PresetSummaryResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class PresetMapper {

    private PresetMapper() {}

    // ───────────── Dockerfile ─────────────
    public static PresetSummaryResponse toSummary(DockerfilePresetDocument d) {
        var m = d.getMeta();
        Objects.requireNonNull(m, "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();
        return new PresetSummaryResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                m.getUpdatedAt(),
                m.getVersion(),
                metrics.getViewed(),
                metrics.getApplied(),
                metrics.getCopied(),
                metrics.getDownloaded()
        );
    }

    public static PresetDetailResponse toDetail(DockerfilePresetDocument d) {
        Map<String, Object> payloadMeta = new LinkedHashMap<>();
        payloadMeta.put("type", "DockerfilePlan");
        payloadMeta.put("hasHealthcheck", d.getPlan() != null && d.getPlan().healthcheck() != null);
        payloadMeta.put("exposeCount", d.getPlan() == null ? 0 : d.getPlan().expose().size());

        var m = d.getMeta();
        Objects.requireNonNull(m, "Meta 정보가 빠져있습니다.");
        var metrics =m.getMetrics();

        return new PresetDetailResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                m.getUpdatedAt(),
                m.getVersion(),
                payloadMeta,
                metrics.getViewed(),
                metrics.getApplied(),
                metrics.getCopied(),
                metrics.getDownloaded()
        );
    }

    // ───────────── Compose(full) ─────────────
    public static PresetSummaryResponse toSummary(ComposePresetDocument d) {
        var m = d.getMeta();
        Objects.requireNonNull(m, "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();
        return new PresetSummaryResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                m.getUpdatedAt(),
                m.getVersion(),
                metrics.getViewed(),
                metrics.getApplied(),
                metrics.getCopied(),
                metrics.getDownloaded()
        );
    }

    public static PresetDetailResponse toDetail(ComposePresetDocument d) {
        Map<String, Object> payloadMeta = new LinkedHashMap<>();
        payloadMeta.put("type", "ComposePlan");
        payloadMeta.put("serviceCount", d.getPlan() == null ? 0 : d.getPlan().services().size());

        var m = d.getMeta();
        Objects.requireNonNull(m, "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();

        return new PresetDetailResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                m.getUpdatedAt(),
                m.getVersion(),
                payloadMeta,
                metrics.getViewed(),
                metrics.getApplied(),
                metrics.getCopied(),
                metrics.getDownloaded()
        );
    }

    // ───────────── Compose(Service piece) ─────────────
    public static PresetSummaryResponse toSummary(ComposeServicePresetDocument d) {
        var m = d.getMeta();
        Objects.requireNonNull(m, "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();
        return new PresetSummaryResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                m.getUpdatedAt(),
                m.getVersion(),
                metrics.getViewed(),
                metrics.getApplied(),
                metrics.getCopied(),
                metrics.getDownloaded()
        );
    }

    public static PresetDetailResponse toDetail(ComposeServicePresetDocument d) {
        Map<String, Object> payloadMeta = new LinkedHashMap<>();
        payloadMeta.put("type", "ComposeService");

        var m = d.getMeta();
        Objects.requireNonNull(m, "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();

        return new PresetDetailResponse(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                m.getUpdatedAt(),
                m.getVersion(),
                payloadMeta,
                metrics.getViewed(),
                metrics.getApplied(),
                metrics.getCopied(),
                metrics.getDownloaded()
        );
    }

}
