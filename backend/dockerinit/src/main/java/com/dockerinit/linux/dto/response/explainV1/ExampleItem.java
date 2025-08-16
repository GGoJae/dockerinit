package com.dockerinit.linux.dto.response.explainV1;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예제 항목")
public record ExampleItem(
        @Schema(description = "예제 명령줄", example = "ping -c 3 google.com")
        String commandLine,

        @Schema(description = "캡션/설명", example = "google.com에 3회 핑")
        String caption
) {}
