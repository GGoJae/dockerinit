package com.dockerinit.features.application.dockercompose.dto.response;

public record ComposePresetResponse(
        String name, String description, String ymlContent
) {
}
