package com.dockerinit.features.application.presetv2.shared.dto.response;

import com.dockerinit.features.application.presetv2.shared.domain.PresetKind;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record PresetDetailResponse(
        String slug,
        String displayName,
        String description,
        PresetKind kind,
        Set<String> tags,
        Boolean deprecated,
        Instant updatedAt,
        Long version,
        Map<String, Object> payloadMeta,   // 예: {"type":"DockerfilePlan","summary":"..."}
        long viewed,
        long applied,
        long copied,
        long downloaded
) {
}
