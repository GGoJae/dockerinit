package com.dockerinit.linux.dto.response.autocompleteV1;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "자동완성 응답 v1")
@JsonInclude(JsonInclude.Include.NON_NULL)

public record LinuxAutocompleteResponse(
        @Schema(description = "API 버전", example = "autocomplete.v2")
        String apiVersion,

        @Schema(description = "명령어 기본 정보")
        BaseInfo base,

        @Schema(description = "커서/토큰 정보")
        CursorInfo cursor,

        @Schema(description = "현재 위치에서 예상되는 토큰 타입 목록(우선순위 포함)")
        List<ExpectedTokenDTO> expected,

        @Schema(description = "SYNOPSIS(패턴/진행상태 포함)")
        SynopsisDTO synopsis,

        @Schema(description = "옵션 메타 정보 (flag -> info)")
        Map<String, OptionDTO> options,

        @Schema(description = "예시 목록")
        List<String> examples,

        @Schema(description = "제안(그룹 단위)")
        SuggestionsBlockDTO suggestions

) {
}
