package com.dockerinit.dto.linuxCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "리눅스 커맨드 생성 요청 객체")
public record LinuxCommandRequest(
        @Schema(description = "실행할 리눅스 명령어", example = "chmod")
        @NotBlank(message = "명령어 이름은 필 수 입니다.")
        String command,

        @Schema(description = "명령어에 전달할 인자 목록", example = "[\"-R\", \"755\"]")
        List<String> args,

        @Schema(description = "명령어가 적용될 대상 파일 또는 디렉토리", example = "/var/www")
        String target
) {}
