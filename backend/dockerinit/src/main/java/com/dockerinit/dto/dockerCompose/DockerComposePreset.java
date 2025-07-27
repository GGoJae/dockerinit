package com.dockerinit.dto.dockerCompose;

public record DockerComposePreset(
        String name, String description, String ymlContent
) {
}
