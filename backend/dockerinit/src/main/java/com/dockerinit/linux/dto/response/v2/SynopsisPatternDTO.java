package com.dockerinit.linux.dto.response.v2;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "시놉시스 한 패턴")
public record SynopsisPatternDTO(
        @Schema(description = "패턴 ID", example = "0") int id,
        @Schema(description = "패턴 라벨", example = "기본") String label,
        @Schema(description = "토큰 칩 목록") List<TokenChipDTO> tokens,
        @Schema(description = "진행 상태") SynopsisProgressDTO progress
) {
}
