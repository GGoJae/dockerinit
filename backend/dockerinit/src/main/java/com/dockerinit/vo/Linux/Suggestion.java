package com.dockerinit.vo.Linux;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자동완성 제안")
public record Suggestion(
        @Schema(description = "실제 입력될 값") String value,
        @Schema(description = "화면에 보여줄 라벨") String display,
        @Schema(description = "추가 설명")         String desc
) {}
