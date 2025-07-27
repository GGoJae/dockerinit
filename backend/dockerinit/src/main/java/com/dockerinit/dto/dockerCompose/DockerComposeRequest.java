package com.dockerinit.dto.dockerCompose;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Docker Compose 생성을 위한 요청 객체")
public record DockerComposeRequest(

        @Schema(description = "사용 언어 또는 프레임워크", example = "springboot")
        String language,

        @Schema(description = "사용할 데이터베이스", example = "postgres")
        String database,

        @Schema(description = "사용할 캐시 시스템", example = "redis")
        String cache,

        @Schema(description = "사용할 메시지 큐", example = "kafka")
        String messageQueue

) {
}
