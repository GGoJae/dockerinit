package com.dockerinit.dto.apiResponse;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class ApiResponse<T> {

    private boolean success;
    private StateCode stateCode;
    private String message;
    private T data;
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
        return new ApiResponse<T>(false, errorCode, message, data);
    }


}
