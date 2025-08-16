package com.dockerinit.linux.dto.response.explainV1;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Service;

@Schema(description = "오퍼랜드 (대상/경로 등)")
public record Operand(
        @Schema(description = "값", example = "google.com")
        String value,

        @Schema(description = "타입", example = "HOST")
        OperandType type,

        @Schema(description = "설명")
        String description
) {}
