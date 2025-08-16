package com.dockerinit.linux.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "설명 요청")
public record ExplainLineRequest (
        @NotBlank String line
) {}
