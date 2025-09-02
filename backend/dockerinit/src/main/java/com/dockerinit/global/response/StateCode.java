package com.dockerinit.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 응답 상태 코드")
public enum StateCode {

    @Schema(description = "요청이 성공적으로 처리됨")
    SUCCESS,

    @Schema(description = "입력값이 잘못됨")
    INVALID_INPUT,

    @Schema(description = "요청한 리소스를 찾을 수 없음")
    NOT_FOUND,

    @Schema(description = "인증되지 않은 사용자 또는 토큰 없음")
    UNAUTHORIZED,

    @Schema(description = "리소스 충돌 발생 (예: 중복된 데이터)")
    CONFLICT,

    @Schema(description = "서버 내부 오류")
    INTERNAL_ERROR,

    @Schema(description = "지원하지 않는 서비스")
    UNSUPPORTED_OPERATION,

    @Schema(description = "생성 완료함")
    CREATED
}
