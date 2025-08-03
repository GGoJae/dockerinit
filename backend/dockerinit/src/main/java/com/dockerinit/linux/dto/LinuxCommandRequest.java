package com.dockerinit.linux.dto;

import com.dockerinit.linux.domain.LinuxCommand;
import com.dockerinit.global.exception.InvalidInputCustomException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_DUPLICATE_FLAG;
import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_REQUIRED_OPTION;

/**
 * 리눅스 명령어(메타데이터) 등록/수정 요청 DTO
 */
@Schema(description = "Linux 명령어 등록·수정 요청")
public record LinuxCommandRequest(

        @Schema(description = "카테고리", example = "네트워크")
        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        @Schema(description = "명령어 이름", example = "ping")
        @NotBlank(message = "command는 필수입니다.")
        String command,

        @Schema(description = "명령어 설명", example = "지정한 호스트에 ICMP 요청을 보내 네트워크 연결 상태를 확인")
        @NotBlank(message = "description은 필수입니다.")
        String description,

        @Schema(description = "사용법(SYNOPSIS)", example = "ping [옵션] 대상호스트")
        @NotBlank(message = "usage는 필수입니다.")
        String usage,

        @Schema(description = "실행 예시", example = "ping -c 3 google.com")
        String example,

        @Schema(description = "검증 여부", defaultValue = "false")
        boolean verified,

        @Schema(description = "이 명령어가 최소 하나의 옵션을 요구하는지 여부", example = "false")
        boolean optionRequired,

        @Schema(description = "옵션 목록")
        @Valid
        List<Option> options,

        @Schema(description = "연관 태그", example = "[\"icmp\", \"연결확인\"]")
        @Size(max = 10, message = "태그는 최대 10개까지만 등록 가능합니다.")
        List<@NotBlank String> tags
) {

    /**
     * 옵션 한 항목
     */
    @Schema(description = "명령어 옵션")
    public record Option(
            @Schema(description = "옵션 플래그", example = "-c")
            @Pattern(regexp = "^-{1,2}\\w[\\w-]*$", message = "플래그 형식이 올바르지 않습니다.")
            String flag,

            @Schema(description = "인수 이름", example = "count")
            @NotBlank(message = "argName은 필수입니다.")
            String argName,

            @Schema(description = "이 옵션이 인수를 필수로 요구하는지 여부", example = "true")
            boolean argRequired,

            @Schema(description = "타입 힌트", example = "int")
            String typeHint,

            @Schema(description = "기본값", example = "null")
            String defaultValue,

            @Schema(description = "옵션 설명", example = "보낼 ping 요청 횟수")
            @NotBlank(message = "description은 필수입니다.")
            String description
    ) {
    }


    public LinuxCommand toEntity() {

        List<Option> optionList = options == null ? List.of() : options;

        if (optionRequired && optionList.isEmpty()) {
            throw new InvalidInputCustomException(LINUX_COMMAND_REQUIRED_OPTION,
                    Map.of("optionRequired", optionRequired, "options", optionList));
        }

        Map<String, LinuxCommand.OptionInfo> optionInfoMap =
                optionList.isEmpty()
                        ? Collections.emptyMap()
                        : optionList.stream().collect(
                        Collectors.toMap(
                                option -> option.flag,
                                option -> new LinuxCommand.OptionInfo(
                                        option.argName(), option.argRequired(), option.typeHint(), option.defaultValue(), option.description()
                                ),
                                (a, b) -> {
                                    throw new InvalidInputCustomException(LINUX_COMMAND_DUPLICATE_FLAG, Map.of("duplicate flag", a.argName()));
                                }
                        )
                );

        return new LinuxCommand(category, command, description, usage, example, verified, optionRequired, optionInfoMap, tags);

    }
}