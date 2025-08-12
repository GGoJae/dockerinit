package com.dockerinit.linux.dto.response.v1;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "명령어 기본 정보")
public record BaseInfo(
        @Schema(description = "명령어", example = "ping") String command,
        @Schema(description = "카테고리", example = "네트워크") String category,
        @Schema(description = "설명") String description,
        @Schema(description = "검증 여부") boolean verified,
        @Schema(description = "태그") List<String> tags,
        @Schema(description = "알 수 없는 명령어 여부") boolean unknown
) {

}
