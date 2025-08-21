package com.dockerinit.features.dockerfile.model;

import org.springframework.http.MediaType;

public record GeneratedFile(
        String filename,
        byte[] content,
        MediaType contentType
) {}
