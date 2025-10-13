package com.dockerinit.features.application.presetV2.dockerfile.mapper;

import com.dockerinit.features.application.presetV2.dockerfile.domain.DockerfilePresetDocument;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetDetailResponseV2;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetSummaryResponseV2;
import com.dockerinit.features.application.presetV2.shared.dto.response.payload.DockerfileMeta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DockerfilePresetMapperV2 {

    private static long nz(Long v) { return v == null ? 0L : v; }
    private static int sizeOf(List<?> l) { return l == null ? 0 : l.size(); }

    public static PresetSummaryResponseV2 toSummary(DockerfilePresetDocument d) {
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

    public static PresetDetailResponseV2 toDetail(DockerfilePresetDocument d) {
        var m = Objects.requireNonNull(d.getMeta(), "Meta 정보가 빠져있습니다.");
        var metrics = m.getMetrics();

        boolean hasHealth = d.getPlan() != null && d.getPlan().healthcheck() != null;
        int exposeCount = (d.getPlan() == null) ? 0 : sizeOf(d.getPlan().expose());

        var payload = new DockerfileMeta(hasHealth, exposeCount);

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
