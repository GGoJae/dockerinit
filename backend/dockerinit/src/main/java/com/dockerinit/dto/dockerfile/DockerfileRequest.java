package com.dockerinit.dto.dockerfile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Schema(description = "Dockerfile 생성을 위한 요청 객체")
public class DockerfileRequest {

    @Schema(description = "사용할 베이스 이미지", example = "openjdk:17")
    @NotNull(message = "기본 이미지(baseImage)는 필수입니다.")
    private String baseImage;

    @Schema(description = "WORKDIR 설정", example = "/app")
    private String workdir;

    @Schema(description = "복사할 파일 목록", example = "[{\"source\":\"src/\", \"target\":\"/app\"}]")
    private List<CopyDirective> copy;

    @Schema(description = "환경 설정 모드", example = "prod")
    private String envMode;

    @Schema(description = "환경변수 설정", example = "{\"SPRING_PROFILES_ACTIVE\":\"prod\"}")
    private Map<String, String> envVars;

    @Schema(description = "EXPOSE 할 포트 리스트", example = "[8080, 443]")
    private List<Integer> expose;

    @Schema(description = "CMD 설정", example = "[\"java\", \"-jar\", \"app.jar\"]")
    @NotEmpty(message = "CMD는 필수입니다.")
    private List<String> cmd;

    @Schema(description = "RUN 명령어 리스트", example = "[\"apt-get update\", \"apt-get install -y curl\"]")
    private List<String> run;

    @Schema(description = "ENTRYPOINT 설정", example = "[\"java\", \"-jar\", \"app.jar\"]")
    private List<String> entrypoint;

    @Schema(description = "LABEL 설정", example = "{\"maintainer\":\"gojae@example.com\"}")
    private Map<String, String> label;

    @Schema(description = "USER 설정", example = "root")
    private String user;

    @Schema(description = "ARG 변수 설정", example = "{\"VERSION\":\"1.0.0\"}")
    private Map<String, String> args;

    @Schema(description = "ADD 명령어를 위한 복사 목록", example = "[{\"source\":\"file.tar.gz\", \"target\":\"/opt/\"}]")
    private List<CopyDirective> add;

    @Schema(description = "Healthcheck 명령어", example = "CMD curl --fail http://localhost:8080 || exit 1")
    private String healthcheck;

    @Schema(description = "볼륨 설정", example = "[\"/data\", \"/var/log\"]")
    private List<String> volume;

    @Schema(description = "파일 복사 지시자 (COPY 또는 ADD 에 사용)")
    public record CopyDirective(
            @Schema(description = "소스 경로", example = "src/")
            @NotBlank(message = "source는 필수입니다.")
            String source,
            @Schema(description = "타겟 경로", example = "/app")
            @NotBlank(message = "target은 필수입니다")
            String target
    ) {
    }
}
