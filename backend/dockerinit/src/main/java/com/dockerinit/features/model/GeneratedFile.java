package com.dockerinit.features.model;

import com.dockerinit.features.dockerfile.domain.DockerFileType;

public record GeneratedFile(
        String filename,
        byte[] content,
        ContentType contentType,
        boolean sensitive,
        DockerFileType fileType
) {}
