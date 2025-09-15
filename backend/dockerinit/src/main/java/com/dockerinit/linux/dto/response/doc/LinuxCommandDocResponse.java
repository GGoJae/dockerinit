package com.dockerinit.linux.dto.response.doc;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "리눅스 명령어 문서 상세 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LinuxCommandDocResponse(

        @Schema(description = "명령어", example = "mv")
        String command,
        @Schema(description = "카테고리", example = "file")
        String category,
        @Schema(description = "별칭(aliases)")
        List<String> aliases,
        @Schema(description = "태그")
        List<String> tags,

        @Schema(description = "설명(DESCRIPTION)")
        String description,

        @Schema(description = "사용 형식(SYNOPSIS) - 렌더링된 1줄")
        String synopsisRendered,
        @Schema(description = "사용 형식(SYNOPSIS) - 구조화 패턴")
        SynopsisDTO synopsis,

        @Schema(description = "검수 완료 여부")
        Boolean verified,
        @Schema(description = "옵션 필수 여부")
        Boolean optionRequired,

        @Schema(description = "주요 인자들")
        List<String> arguments,
        @Schema(description = "예시들")
        List<String> examples,

        @Schema(description = "옵션 목록")
        List<OptionDTO> options,

        @Schema(description = "검색 카운트")
        Long searchCount,

        @Schema(description = "수집 메타 정보")
        SourceMeta meta
) {
    @Schema(description = "시놉시스(패턴) DTO")
    public record SynopsisDTO(
            @Schema(description = "패턴 목록")
            List<PatternDTO> patterns
    ) {
        @Schema(description = "단일 패턴")
        public record PatternDTO(
                @Schema(description = "토큰들")
                List<TokenDTO> tokens
        ) {}

        @Schema(description = "토큰")
        public record TokenDTO(
                @Schema(description = "토큰 타입", example = "LITERAL|FLAG|ARG")
                String type,
                @Schema(description = "표시 문자열", example = "--force 또는 FILE")
                String text,
                @Schema(description = "선택 여부([] 구문에 해당)")
                Boolean optional,
                @Schema(description = "반복 여부(.../+)에 해당")
                Boolean repeat,
                @Schema(description = "설명(있다면)")
                String description
        ) {}
    }

    @Schema(description = "옵션 DTO")
    public record OptionDTO(
            @Schema(description = "옵션 플래그(대표 키)", example = "--force")
            String flag,
            @Schema(description = "인자 이름", example = "FILE")
            String argName,
            @Schema(description = "인자 필수 여부")
            Boolean argRequired,
            @Schema(description = "타입 힌트", example = "path|string|number")
            String typeHint,
            @Schema(description = "기본값")
            String defaultValue,
            @Schema(description = "설명")
            String description
    ) {}

    @Schema(description = "원문/수집 메타")
    public record SourceMeta(
            @Schema(description = "섹션", example = "1")
            String section,
            @Schema(description = "배포판", example = "ubuntu-24.04")
            String distro,
            @Schema(description = "출처", example = "local-man")
            String source,
            @Schema(description = "원문 해시(SHA-256)")
            String sourceHash,
            @Schema(description = "수집 시각")
            Instant importedAt
    ) {}
}
