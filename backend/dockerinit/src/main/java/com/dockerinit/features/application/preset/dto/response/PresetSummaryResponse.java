package com.dockerinit.features.application.preset.dto.response;

import com.dockerinit.features.application.preset.dto.spec.PresetKindDTO;

import java.time.Instant;
import java.util.Set;

public record PresetSummaryResponse(
        String id,
        String slug,
        String displayName,
        PresetKindDTO presetKind,
        Set<String> tags,
        boolean deprecated,
        Long downloadCount,
        Instant updatedAt
) {
}
