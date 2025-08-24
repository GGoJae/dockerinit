package com.dockerinit.features.dockerfile.model;

public record GeneratedFile(
        String filename,
        byte[] content,
        ContentType contentType,
        boolean sensitive,
        FileType fileType
) {}
