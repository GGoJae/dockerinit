package com.dockerinit.features.preset.dto.response;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.preset.dto.spec.ContentStrategyDTO;

public record PresetArtifactMetaResponse(
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
