package com.dockerinit.dto.apiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Schema(description = "공통 API 응답 객체")
public class ApiResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 상태 코드 (성공/에러 등)")
    private StateCode stateCode;

    @Schema(description = "응답 메시지", example = "성공적으로 처리되었습니다.")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    @Schema(description = "응답 시간 (KST)", example = "2025-07-28T15:00:00+09:00")
    private ZonedDateTime timestamp;

    public ApiResponse(boolean success, StateCode stateCode, String message, T data) {
        this.success = success;
        this.stateCode = stateCode;
        this.message = message;
        this.data = data;
        this.timestamp = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, StateCode.SUCCESS, "성공정으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, StateCode.SUCCESS, message, data);
    }

    public static <T> ApiResponse<T> error(StateCode errorCode, String message) {
        return new ApiResponse<>(false, errorCode, message, null);
    }

    public static <T> ApiResponse<T> error(StateCode errorCode, String message, T data) {
        return new ApiResponse<>(false, errorCode, message, data);
    }

    // Getter 생략 또는 @Getter 추가 (Swagger에는 영향 없음)
}
