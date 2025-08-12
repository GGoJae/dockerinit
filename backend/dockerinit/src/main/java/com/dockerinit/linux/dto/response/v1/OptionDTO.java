package com.dockerinit.linux.dto.response.v1;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "옵션 메타 정보")
public record OptionDTO(
        @Schema(description = "인자 이름", example = "count") String argName,
        @Schema(description = "인자 필수 여부") boolean argRequired,
        @Schema(description = "타입 힌트", example = "int") String typeHint,
        @Schema(description = "기본값") String defaultValue,
        @Schema(description = "설명") String description
) {
}
