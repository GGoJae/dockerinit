package com.dockerinit.features.dockercompose.dto.request;

import com.dockerinit.features.support.validation.composeService.ExactlyOneOfImageOrBuild;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;
import java.util.Map;

public record ComposeRequestV1(

        @Schema(description = "Compose 프로젝트 이름", example = "myapp")
        String projectName,

        @Schema(description = "서비스 목록")
        @Size(min = 1, max = 50, message = "서비스는 1~50개")
        List<@Valid Service> services,

        @Schema(description = "네트워크 정의")
        Map<String, @Valid Network> networks,

        @Schema(description = "볼륨 정의")
        Map<String, @Valid Volume> volumes
) {

    @Schema(description = "서비스 정의")
    @ExactlyOneOfImageOrBuild
    public record Service(
            @Schema(description = "서비스 이름", example = "app")
            @NotBlank String name,

            @Schema(description = "이미지명(또는 build와 택1)", example = "eclipse-temurin:17-jre")
            String image,

            @Schema(description = "빌드 설정(또는 image와 택1)")
            @Valid Build build,

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
            @Valid Healthcheck healthcheck
    ) {}

    @Schema(description = "빌드 설정")
    public record Build(
            @Schema(description = "빌드 컨텍스트", example = ".")
            @NotBlank String context,

            @Schema(description = "Dockerfile 경로", example = "Dockerfile")
            String dockerfile,

            @Schema(description = "빌드 인자")
            Map<String,String> args
    ) {}

    @Schema(description = "네트워크 정의")
    public record Network(
            @Schema(description = "드라이버", example = "bridge")
            String driver
    ) {}

    @Schema(description = "볼륨 정의")
    public record Volume(
            @Schema(description = "드라이버", example = "local")
            String driver
    ) {}

    @Schema(description = "헬스체크")
    public record Healthcheck(
            @Schema(description = "테스트 명령", example = "curl --fail http://app:8080/actuator/health || exit 1")
            @NotBlank String test,
            @Schema(description = "주기", example = "30s") String interval,
            @Schema(description = "타임아웃", example = "3s") String timeout,
            @Schema(description = "재시도", example = "3") Integer retries,
            @Schema(description = "시작 유예", example = "10s") String startPeriod
    ) {}
}
