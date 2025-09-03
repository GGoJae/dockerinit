package com.dockerinit.features.dockercompose.dto.spec;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@Schema(description = "빌드 설정")
public record BuildDTO(
        @Schema(description = "빌드 컨텍스트", example = ".")
        @NotBlank String context,

        @Schema(description = "Dockerfile 경로", example = "Dockerfile")
        String dockerfile,

        @Schema(description = "빌드 인자")
        Map<String,String> args
) {}
