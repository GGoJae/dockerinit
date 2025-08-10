package com.dockerinit.linux.dto.response.v2;

import com.dockerinit.linux.domain.syntax.TokenType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시놉시스 토큰 칩")
public record TokenChipDTO(
        @Schema(description = "토큰 타입") TokenType type,
        @Schema(description = "선택적 여부") boolean optional,
        @Schema(description = "반복 가능 여부") boolean repeat,
        @Schema(description = "설명(표시용)", example = "HOST / DEST")
        String description
) {
}
