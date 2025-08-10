package com.dockerinit.linux.dto.response.v2;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "제안 그룹")
public record SuggestionGroupDTO(
        @Schema(description = "그룹 라벨(예: OPTION/ARGUMENT/TARGET)") String group,
        @Schema(description = "그룹 내 아이템") List<SuggestionV2> items
) {
}
