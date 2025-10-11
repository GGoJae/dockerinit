package com.dockerinit.features.application.presetv2.shared.dto.response;

import java.time.Instant;
import java.util.List;

public record PresetArtifactResponse(
        String slug,
        List<String> artifactFilenames,
        Instant updatedAt
) {
}
