package com.dockerinit.linux.dto.response.v1;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "커서/토큰 정보")
public record CursorInfo(
        @Schema(description = "원본 라인", example = "ping -c ")
        String line,
        @Schema(description = "커서 위치(문자 인덱스)", example = "7")
        int cursor,
        @Schema(description = "현재 토큰(커서가 있는 토큰)", example = "")
        String currentToken,
        @Schema(description = "토큰 인덱스", example = "2")
        int tokenIndex,
        @Schema(description = "직전 플래그", example = "-c")
        String prevFlag
) {
    
}
