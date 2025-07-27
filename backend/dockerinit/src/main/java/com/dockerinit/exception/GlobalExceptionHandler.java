package com.dockerinit.exception;

import com.dockerinit.dto.apiResponse.ApiResponse;
import com.dockerinit.dto.apiResponse.StateCode;
import com.dockerinit.exception.CustomException.CustomApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomApiException(CustomApiException ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.error(ex.getErrorCode(), ex.getMessage(), ex.getData())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleGeneralException(Exception ex) {
        return ResponseEntity.internalServerError().body(
                ApiResponse.error(
                        StateCode.INTERNAL_ERROR, "서버 오류가 발생했습니다."
                )
        );
    }
}
