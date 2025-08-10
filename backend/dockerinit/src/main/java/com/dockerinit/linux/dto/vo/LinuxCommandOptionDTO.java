package com.dockerinit.linux.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 옵션 한 항목
 */
@Schema(description = "명령어 옵션")
public record LinuxCommandOptionDTO(

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
    ) {}

