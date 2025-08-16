package com.dockerinit.linux.dto.response.explainV1;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "헤더/요청 정보")
public record Header(
        @Schema(description = "명령어", example = "ping")
        String command,

        @Schema(description = "한 줄 요약", example = "지정한 호스트로 ICMP 요청을 보내 네트워크 상태를 점검합니다.")
        String summary,

        @Schema(description = "태그", example = "[\"네트워크\", \"icmp\"]")
        List<String> tags
) {}
