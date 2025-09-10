package com.dockerinit.features.application.preset.dto.spec;

import com.dockerinit.features.model.FileType;

public record PresetArtifactMetaDTO(
        FileType fileType,
        String filename,
        String contentType,
        ContentStrategyDTO strategy,
        Boolean sensitive,
        String etag,
        Long contentLength,
        String storageProvider,
        String storageKey
) {
}
