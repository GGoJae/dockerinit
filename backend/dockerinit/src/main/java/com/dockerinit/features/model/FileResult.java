package com.dockerinit.features.model;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

public record FileResult(
        Resource resource,
        long contentLength,
        String filename,
        MediaType contentType,
        @Nullable
        String eTag
) {}
