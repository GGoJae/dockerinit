package com.dockerinit.features.application.presetV2.shared.dto.response;

import com.dockerinit.features.application.presetV2.shared.domain.PresetKind;
import com.dockerinit.features.application.presetV2.shared.dto.response.payload.PresetPayloadMeta;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PresetDetailResponseV2(
        String slug,
        String displayName,
        String description,
        PresetKind kind,
        Set<String> tags,
        Boolean deprecated,
        Instant updatedAt,
        Long version,
        PresetPayloadMeta payload,
        long viewed,
        long applied,
        long copied,
        long downloaded
) {
}
