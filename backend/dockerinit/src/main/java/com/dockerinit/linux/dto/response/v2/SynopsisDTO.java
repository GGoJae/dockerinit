package com.dockerinit.linux.dto.response.v2;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "시놉시스 전체")
public record SynopsisDTO(
        @Schema(description = "활성 패턴 ID(선택)") Integer activePatternId,
        @Schema(description = "커맨드 이후 위치(0-based)", example = "1") int position,
        @Schema(description = "시놉시스 패턴들") List<SynopsisPatternDTO> patterns
) {
}
