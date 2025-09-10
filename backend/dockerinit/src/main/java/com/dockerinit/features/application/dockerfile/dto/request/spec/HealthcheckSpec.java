package com.dockerinit.features.application.dockerfile.dto.request.spec;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Healthcheck 구조")
public record HealthcheckSpec(
        @Schema(description = "헬스체크 명령 전체", example = "CMD curl --fail http://localhost:8080 || exit 1")
        @NotBlank String cmd,

        @Schema(description = "체크 간격", example = "30s") // 필요하면 정규식 강화
        @Pattern(regexp = "^[0-9]+(ms|s|m|h)$", message = "interval 형식: 10ms|10s|10m|10h")
        String interval,

        @Schema(description = "타임아웃", example = "3s")
        @Pattern(regexp = "^[0-9]+(ms|s|m|h)$", message = "timeout 형식: 10ms|10s|10m|10h")
        String timeout,

        @Schema(description = "재시도 횟수", example = "3")
        @Min(1) @Max(100)
        Integer retries,

        @Schema(description = "시작 유예기간", example = "10s")
        @Pattern(regexp = "^[0-9]+(ms|s|m|h)$", message = "startPeriod 형식: 10ms|10s|10m|10h")
        String startPeriod
) {}
