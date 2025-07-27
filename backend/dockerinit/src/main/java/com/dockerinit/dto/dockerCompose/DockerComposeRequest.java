package com.dockerinit.dto.dockerCompose;

public record DockerComposeRequest(
        String language,
        String database,
        String cache,
        String messageQueue
) {
}
