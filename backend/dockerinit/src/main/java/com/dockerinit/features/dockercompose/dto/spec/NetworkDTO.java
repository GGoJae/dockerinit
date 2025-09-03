package com.dockerinit.features.dockercompose.dto.spec;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "네트워크 정의")
public record NetworkDTO(
        @Schema(description = "드라이버", example = "bridge")
        String driver
) {}
