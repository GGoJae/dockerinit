package com.dockerinit.features.dockerfile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dockerfile 프리셋 정보")
public record DockerfilePresetRequest(

        @Schema(description = "프리셋 이름", example = "springboot-java17")
        String name,

        @Schema(description = "Dockerfile 내용 (YAML 형식 문자열)", example = "FROM openjdk:17\nWORKDIR /app\nCOPY . .\nCMD [\"java\", \"-jar\", \"app.jar\"]")
        String dockerfile

) {}
