package com.dockerinit.linux.dto.response.v2;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시놉시스 진행 상태")
public record SynopsisProgressDTO(
        @Schema(description = "채워진 토큰 수") int filledCount,
        @Schema(description = "남은 필수 토큰 수") int requiredRemaining,
        @Schema(description = "남은 선택 토큰(대략)") int optionalRemaining
) {
}
