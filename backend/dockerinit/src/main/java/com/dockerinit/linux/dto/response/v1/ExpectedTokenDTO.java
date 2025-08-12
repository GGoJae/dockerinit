package com.dockerinit.linux.dto.response.v1;

import com.dockerinit.linux.domain.syntax.TokenType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "예상 토큰 타입(우선순위/신뢰도 포함)")
public record ExpectedTokenDTO(
        @Schema(description = "토큰 타입") TokenType type,
        @Schema(description = "우선순위(낮을수록 우선)", example = "0") int priority,
        @Schema(description = "신뢰도(0.0~1.0+)", example = "1.0") double confidence,
        @Schema(description = "부가 메타데이터(prevFlag 등)") Map<String, Object> meta
) {
}
