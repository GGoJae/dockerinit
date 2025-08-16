package com.dockerinit.linux.dto.response.autocompleteV1;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "제안 블록(그룹 단위)")
public record SuggestionsBlockDTO(
        @Schema(description = "제안 그룹들") List<SuggestionGroupDTO> groups,
        @Schema(description = "최대 개수") int limit
) {
}
