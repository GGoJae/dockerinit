package com.dockerinit.features.dockerfile.model;

import com.dockerinit.features.dockerfile.dto.request.DockerfileRequest;

import java.util.List;
import java.util.Set;

public record RenderContext(
        DockerfileRequest req,
        DockerfilePlan plan,
        Set<FileType> targets,
        List<GeneratedFile> untilNowArtifacts
) {}
