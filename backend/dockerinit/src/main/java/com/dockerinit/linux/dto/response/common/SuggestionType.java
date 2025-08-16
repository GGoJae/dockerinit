package com.dockerinit.linux.dto.response.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자동완성 제안 타입")
public enum SuggestionType {
    @Schema(description = "명령어")
    COMMAND,
    @Schema(description = "옵션")
    OPTION,
    @Schema(description = "옵션의 인자")
    ARGUMENT,
    @Schema(description = "파일, 디렉토리 등의 대상")
    TARGET
}
