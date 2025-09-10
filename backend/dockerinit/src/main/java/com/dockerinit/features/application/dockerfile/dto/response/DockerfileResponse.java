package com.dockerinit.features.application.dockerfile.dto.response;

import java.util.List;

public record DockerfileResponse(
        String dockerfile,
        List<String> warnings) {
}
