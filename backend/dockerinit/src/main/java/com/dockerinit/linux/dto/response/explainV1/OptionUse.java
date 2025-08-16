package com.dockerinit.linux.dto.response.explainV1;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "옵션 사용 정보")
public record OptionUse(
        @Schema(description = "옵션 플래그", example = "-c")
        String flag,

        @Schema(description = "인자 이름(메타)", example = "count")
        String argName,

        @Schema(description = "실제 값", example = "3")
        String value,

        @Schema(description = "인자 필수 여부(메타)")
        boolean argRequired,

        @Schema(description = "옵션 설명(메타)")
        String description
) {}
