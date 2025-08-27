package com.dockerinit.features.dockerfile.dto.response;

import java.util.List;

public record DockerfileResponse(
        String dockerfile,
        List<String> warnings) {
}
