package com.dockerinit.linux.dto.response.explainV1;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주의/안내 노트")
public record Note(
        @Schema(description = "레벨", example = "WARNING")
        NoteLevel level,

        @Schema(description = "메시지", example = "주의: 재귀 삭제(-r)는 되돌릴 수 없습니다.")
        String message
) {}
