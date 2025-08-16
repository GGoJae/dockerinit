package com.dockerinit.linux.dto.response.autocompleteV1;

import com.dockerinit.linux.dto.response.common.SuggestionType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자동완성 제안")
public record Suggestion(
        @Schema(description = "실제 입력될 값") String value,
        @Schema(description = "보여줄 라벨") String display,
        @Schema(description = "설명/툴팁") String desc,
        @Schema(description = "제안 타입") SuggestionType type,
        @Schema(description = "랭킹/스코어", example = "0.95") Double score,
        @Schema(description = "치환 범위 시작 오프셋(선택)") Integer replaceStart,
        @Schema(description = "치환 범위 끝 오프셋(선택)") Integer replaceEnd
) {}
