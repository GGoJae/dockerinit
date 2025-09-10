package com.dockerinit.features.application.preset.dto.request;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.application.preset.dto.spec.ContentStrategyDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresetArtifactRequest(
        @NotNull FileType fileType,
        @NotBlank String filename,
        @NotBlank String contentType,       // MIME string (e.g., "text/plain; charset=utf-8")
        @NotNull ContentStrategyDTO strategy, // INLINE or OBJECT

        // INLINE 전용
        String inlineContent,

        // OBJECT 전용
        String storageProvider,   // e.g., "s3", "gcs", "minio"
        String storageKey,        // e.g., "presets/<slug>/Dockerfile"

        // 공통 메타
        Boolean sensitive,
        String etag,
        @Min(0) Long contentLength
) {
}
