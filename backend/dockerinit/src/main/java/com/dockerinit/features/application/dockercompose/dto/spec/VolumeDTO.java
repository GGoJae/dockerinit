package com.dockerinit.features.application.dockercompose.dto.spec;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "볼륨 정의")
public record VolumeDTO(
        @Schema(description = "드라이버", example = "local")
        String driver
) {}
