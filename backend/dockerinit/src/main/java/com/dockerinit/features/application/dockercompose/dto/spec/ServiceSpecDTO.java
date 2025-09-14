package com.dockerinit.features.application.dockercompose.dto.spec;

import com.dockerinit.global.validation.composeService.ExactlyOneOfImageOrBuild;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@Schema(description = "서비스 정의")
@ExactlyOneOfImageOrBuild
public record ServiceSpecDTO(
        @Schema(description = "서비스 이름", example = "app")
        @NotBlank String name,

        @Schema(description = "이미지명(또는 build와 택1)", example = "eclipse-temurin:17-jre")
        String image,

        @Schema(description = "빌드 설정(또는 image와 택1)")
        @Valid BuildDTO build,

        @Schema(description = "실행 커맨드", example = "[\"java\",\"-jar\",\"app.jar\"]")
        List<String> command,

        @Schema(description = "환경변수", example = "{\"SPRING_PROFILES_ACTIVE\":\"prod\"}")
        Map<String,String> environment,

        @Schema(description = "env 파일 목록", example = "[\".env\"]")
        List<String> envFile,

        @Schema(description = "포트 매핑", example = "[\"8080:8080\"]")
        List<String> ports,

        @Schema(description = "볼륨 마운트", example = "[\"./data:/data\"]")
        List<String> volumes,

        @Schema(description = "의존 서비스", example = "[\"db\"]")
        List<String> dependsOn,

        @Schema(description = "재시작 정책", example = "always")
        String restart,

        @Schema(description = "헬스체크")
        @Valid HealthcheckDTO healthcheck
) {}
