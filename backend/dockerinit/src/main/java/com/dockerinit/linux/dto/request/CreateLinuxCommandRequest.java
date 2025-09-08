package com.dockerinit.linux.dto.request;

import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.Synopsis;
import com.dockerinit.linux.domain.syntax.SynopsisPattern;
import com.dockerinit.linux.domain.syntax.TokenDescriptor;
import com.dockerinit.linux.dto.request.spec.OptionSpecDTO;
import com.dockerinit.linux.dto.request.spec.SynopsisPatternSpecDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.*;

import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_DUPLICATE_FLAG;
import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_REQUIRED_OPTION;

/**
 * 리눅스 명령어(메타데이터) 등록 요청 DTO
 */
@Schema(description = "Linux 명령어 등록 요청")
public record CreateLinuxCommandRequest(

        @Schema(description = "카테고리", example = "네트워크")
        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        @Schema(description = "명령어 이름", example = "ping")
        @NotBlank(message = "command는 필수입니다.")
        String command,

        @Schema(description = "명령어 설명", example = "지정한 호스트에 ICMP 요청을 보내 네트워크 연결 상태를 확인")
        @NotBlank(message = "description은 필수입니다.")
        String description,

        @Schema(description = "별칭들", example = "")
        List<String> aliases,

        // TODO example 복잡하니.. 정리가 된 후에 작성하기
        @Schema(description = "사용법(SYNOPSIS)", example = "")
        @NotEmpty(message = "synopsis는 필수입니다.")
        List<@Valid SynopsisPatternSpecDTO> synopsis,

        @Schema(description = "주요 인자 목록", example = "[\"HOST\"]")
        @Size(max = 10, message = "인자는 최대 10개까지만 등록 가능합니다.")
        List<@NotBlank String> arguments,

        @Schema(description = "예시 목록", example = "[\"ping -c 3 google.com\"]")
        List<@NotBlank String> examples,

        @Schema(description = "검증 여부", defaultValue = "false")
        boolean verified,

        @Schema(description = "이 명령어가 최소 하나의 옵션을 요구하는지 여부", example = "false")
        boolean optionRequired,

        @Schema(description = "옵션 목록")
        @Valid
        List<OptionSpecDTO> options,

        @Schema(description = "연관 태그", example = "[\"icmp\", \"연결확인\"]")
        @Size(max = 10, message = "태그는 최대 10개까지만 등록 가능합니다.")
        List<@NotBlank String> tags
) {
}