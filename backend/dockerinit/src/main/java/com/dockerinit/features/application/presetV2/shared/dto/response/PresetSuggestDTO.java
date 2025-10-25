package com.dockerinit.features.application.presetV2.shared.dto.response;

import com.dockerinit.features.application.presetV2.shared.domain.PresetKind;

import java.time.Instant;
import java.util.Set;

public record PresetSuggestDTO(
        String slug,
        String displayName,
        PresetKind kind,
        Set<String> tags,
        Boolean deprecated
) {
}
