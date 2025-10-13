package com.dockerinit.features.application.presetV2.shared.dto.response.payload;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type")
public sealed interface PresetPayloadMeta permits DockerfileMeta, ComposeMeta, ComposeServiceMeta{
}
