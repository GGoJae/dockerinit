package com.dockerinit.dockercompose.dto;

public record DockerComposePreset(
        String name, String description, String ymlContent
) {
}
