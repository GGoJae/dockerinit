package com.dockerinit.features.dockercompose.dto.response;

public record ComposePresetResponse(
        String name, String description, String ymlContent
) {
}
