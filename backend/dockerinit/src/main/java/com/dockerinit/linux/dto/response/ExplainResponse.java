package com.dockerinit.linux.dto.response;

import com.dockerinit.linux.dto.response.explainV1.Details;
import com.dockerinit.linux.dto.response.explainV1.ExampleItem;
import com.dockerinit.linux.dto.response.explainV1.Header;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "설명 응답")
public record ExplainResponse(
        @Schema(description = "API 버전", example = "explain.v2")
        String apiVersion,

        @Schema(description = "기본 정보들")
        Header header,

        @Schema(description = "상세 정보들")
        Details details,

        @Schema(description = "예시 항목들")
        List<ExampleItem> examples,

        @Schema(description = "캐시로부터 가져왔는지 여부")
        boolean fromCache
) {}
