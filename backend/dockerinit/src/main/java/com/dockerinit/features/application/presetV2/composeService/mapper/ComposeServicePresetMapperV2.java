package com.dockerinit.features.application.presetV2.composeService.mapper;

import com.dockerinit.features.application.presetV2.composeService.domain.ComposeServicePresetDocument;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetDetailResponseV2;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetSummaryResponseV2;
import com.dockerinit.features.application.presetV2.shared.dto.response.payload.ComposeServiceMeta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComposeServicePresetMapperV2 {

    private static long nz(Long v) { return v == null ? 0L : v; }

    public static PresetSummaryResponseV2 toSummary(ComposeServicePresetDocument d) {
        var m = Objects.requireNonNull(d.getMeta(), "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();
        return new PresetSummaryResponseV2(
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

    public static PresetDetailResponseV2 toDetail(ComposeServicePresetDocument d) {
        var m = Objects.requireNonNull(d.getMeta(), "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();

        var payload = new ComposeServiceMeta();

        return new PresetDetailResponseV2(
                m.getSlug(),
                m.getDisplayName(),
                m.getDescription(),
                d.getKind(),
                m.getTags(),
                m.getDeprecated(),
                d.getUpdatedAt(),
                d.getVersion(),
                payload,
                nz(metrics.getViewed()),
                nz(metrics.getApplied()),
                nz(metrics.getCopied()),
                nz(metrics.getDownloaded())
        );
    }
}
