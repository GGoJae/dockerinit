package com.dockerinit.features.application.preset.dto.response;

import com.dockerinit.features.application.preset.dto.spec.PresetArtifactMetaDTO;

import java.time.Instant;
import java.util.List;

public record PresetArtifactResponse(
        List<PresetArtifactMetaDTO> artifacts,
        Instant updatedAt
) {
}
