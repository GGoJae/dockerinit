package com.dockerinit.linux.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자동완성 제안")
public record Suggestion(

        @Schema(description = "실제 입력될 값", example = "ping")
        String value,

        @Schema(description = "화면에 보여줄 라벨", example = "ping")
        String display,

        @Schema(description = "추가 설명", example = "네트워크 상태 확인 명령어")
        String desc,

        @Schema(description = "제안 타입")
        SuggestionType type
) {
}
