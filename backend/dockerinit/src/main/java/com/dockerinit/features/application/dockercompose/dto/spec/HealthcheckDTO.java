package com.dockerinit.features.application.dockercompose.dto.spec;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "헬스체크")
public record HealthcheckDTO(
        @Schema(description = "테스트 명령", example = "curl --fail http://app:8080/actuator/health || exit 1")
        @NotBlank String test,
        @Schema(description = "주기", example = "30s") String interval,
        @Schema(description = "타임아웃", example = "3s") String timeout,
        @Schema(description = "재시도", example = "3") Integer retries,
        @Schema(description = "시작 유예", example = "10s") String startPeriod
) {}
