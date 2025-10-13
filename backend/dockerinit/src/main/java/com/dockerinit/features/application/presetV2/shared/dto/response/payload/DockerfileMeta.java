package com.dockerinit.features.application.presetV2.shared.dto.response.payload;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("dockerfile")
public record DockerfileMeta(
        boolean hasHealthcheck,
        int exposeCount
) implements PresetPayloadMeta{
}
