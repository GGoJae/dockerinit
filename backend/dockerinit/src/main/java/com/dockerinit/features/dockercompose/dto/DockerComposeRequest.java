package com.dockerinit.features.dockercompose.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Docker Compose 생성을 위한 요청 객체")
public record DockerComposeRequest(

        @Schema(description = "사용 언어 또는 프레임워크", example = "springboot")
                @NotNull(message = "언어(language)는 필수입니다.")
        String language,

        @Schema(description = "사용할 데이터베이스", example = "postgres")
                @NotBlank(message = "DB 타입은 필수입니다.")
        String database,

        @Schema(description = "사용할 캐시 시스템", example = "redis")
        String cache,

        @Schema(description = "사용할 메시지 큐", example = "kafka")
        String messageQueue

) {
}
