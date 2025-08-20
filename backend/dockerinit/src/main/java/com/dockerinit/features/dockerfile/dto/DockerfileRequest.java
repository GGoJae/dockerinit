package com.dockerinit.features.dockerfile.dto;

import com.dockerinit.features.support.validation.DockerfileCrossCheck;
import com.dockerinit.features.support.validation.SafeRelPath;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;

@Schema(description = "Dockerfile 생성을 위한 요청 객체")
@DockerfileCrossCheck // CMD/ENTRYPOINT, WORKDIR 등 교차검증
public record DockerfileRequest(

        @Schema(description = "사용할 베이스 이미지", example = "openjdk:17")
        @NotBlank(message = "기본 이미지(baseImage)는 필수입니다.")
        String baseImage,

        @Schema(description = "WORKDIR 설정(절대경로 권장)", example = "/app")
        String workdir,

        @Schema(description = "COPY 지시자 목록", example = "[{\"source\":\"src/\", \"target\":\"/app\"}]")
        @Size(max = 50, message = "copy 지시자는 최대 50개까지 허용됩니다.")
        List<@Valid CopyDirective> copy,

        @Schema(description = "ADD 지시자 목록", example = "[{\"source\":\"file.tar.gz\", \"target\":\"/opt/\"}]")
        @Size(max = 50, message = "add 지시자는 최대 50개까지 허용됩니다.")
        List<@Valid CopyDirective> add,

        @Schema(description = "환경 설정 모드", example = "prod")
        EnvModeDTO envMode,

        @Schema(description = "환경변수 설정", example = "{\"SPRING_PROFILES_ACTIVE\":\"prod\"}")
        @Size(max = 100, message = "envVars는 최대 100개까지 허용됩니다.")
        Map<
                @Pattern(regexp = "^[A-Z_][A-Z0-9_]*$", message = "ENV 키는 대문자/숫자/언더스코어만 허용")
                        String,
                @NotBlank(message = "ENV 값은 공백일 수 없습니다.")
                        String
                > envVars,

        @Schema(description = "EXPOSE 할 포트 리스트", example = "[8080, 443]")
        @Size(max = 50, message = "expose 포트는 최대 50개까지 허용됩니다.")
        List<@NotNull @Min(1) @Max(65535) Integer> expose,

        @Schema(description = "CMD 설정", example = "[\"java\", \"-jar\", \"app.jar\"]")
        @Size(max = 50, message = "cmd 항목은 최대 50개까지 허용됩니다.")
        List<@NotBlank String> cmd,

        @Schema(description = "RUN 명령어 리스트", example = "[\"apt-get update\", \"apt-get install -y curl\"]")
        @Size(max = 200, message = "run 명령은 최대 200개까지 허용됩니다.")
        List<@NotBlank String> run,

        @Schema(description = "ENTRYPOINT 설정", example = "[\"java\", \"-jar\", \"app.jar\"]")
        @Size(max = 50, message = "entrypoint 항목은 최대 50개까지 허용됩니다.")
        List<@NotBlank String> entrypoint,

        @Schema(description = "LABEL 설정", example = "{\"maintainer\":\"gojae@example.com\"}")
        @Size(max = 100, message = "label은 최대 100개까지 허용됩니다.")
        Map<@NotBlank String, @NotBlank String> label,

        @Schema(description = "USER 설정", example = "root")
        @Size(max = 100, message = "user 값이 너무 깁니다.")
        String user,

        @Schema(description = "ARG 변수 설정", example = "{\"VERSION\":\"1.0.0\"}")
        @Size(max = 100, message = "args는 최대 100개까지 허용됩니다.")
        Map<
                @Pattern(regexp = "^[A-Z_][A-Z0-9_]*$", message = "ARG 키는 대문자/숫자/언더스코어만 허용")
                        String,
                @NotBlank String
                > args,

        @Schema(description = "Healthcheck 설정(선택)")
        @Valid Healthcheck healthcheck,

        @Schema(description = "볼륨 설정(절대경로)", example = "[\"/data\", \"/var/log\"]")
        @Size(max = 50, message = "volume은 최대 50개까지 허용됩니다.")
        List<
                @Pattern(regexp = "^/.*$", message = "볼륨은 절대경로여야 합니다.")
                        String
                > volume
) {

    public enum EnvModeDTO { dev, staging, prod }

    @Schema(description = "파일 복사 지시자 (COPY/ADD 용)")
    public record CopyDirective(
            @Schema(description = "소스 경로(상대경로 권장)", example = "src/")
            @NotBlank(message = "source는 필수입니다.")
            @SafeRelPath // 상대경로만 허용, '..' / URL 금지
            String source,

            @Schema(description = "타겟 경로(절대경로)", example = "/app")
            @NotBlank(message = "target은 필수입니다.")
            @Pattern(regexp = "^/.*$", message = "target은 절대경로여야 합니다.")
            String target
    ) {}

    @Schema(description = "Healthcheck 구조")
    public record Healthcheck(
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
}
