package com.dockerinit.features.model;

public record GeneratedFile(
        String filename,
        byte[] content,
        ContentType contentType,
        boolean sensitive,
        FileType fileType
) {}
