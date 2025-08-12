package com.dockerinit.features.dockercompose.dto;

public record DockerComposePreset(
        String name, String description, String ymlContent
) {
}
