package com.dockerinit.linux.dto.response.explainV1;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "상세 정보(옵션/대상")
public record Details (

        @Schema(description = "사용된 옵션 목록")
        List<OptionUse> options,

        @Schema(description = "오퍼랜드(대상/경로 등)")
        List<Operand> operands,

        @Schema(description = "주의/안내 노트")
        List<Note> notes
) {}
