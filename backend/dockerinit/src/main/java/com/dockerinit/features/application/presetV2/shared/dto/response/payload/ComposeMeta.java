package com.dockerinit.features.application.presetV2.shared.dto.response.payload;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("compose")
public record ComposeMeta(
        int ServiceCount
) implements PresetPayloadMeta{
}
