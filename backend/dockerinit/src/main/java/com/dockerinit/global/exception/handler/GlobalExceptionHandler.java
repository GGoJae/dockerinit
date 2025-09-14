package com.dockerinit.global.exception.handler;

import com.dockerinit.global.constants.ErrorMessage;
import com.dockerinit.global.exception.CustomApiException;
import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.exception.model.ErrorContent;
import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.global.response.StateCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomApiException(CustomApiException ex) {
        return ResponseEntity
                .status(toHttpStatus(ex.getErrorCode()))
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage(), ex.getData()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, List<ErrorContent>> errorMap = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        fe -> fe.getField(),
                        LinkedHashMap::new,
                        Collectors.mapping(fe ->
                                ErrorContent.of(fe.getDefaultMessage(), safeToString(fe.getField(), fe.getRejectedValue())),
                                Collectors.toList())
                ));

        return ResponseEntity.badRequest().body(
                ApiResponse.error(StateCode.INVALID_INPUT, ErrorMessage.INVALID_INPUT, errorMap)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        return ResponseEntity.internalServerError().body(
                ApiResponse.error(
                        StateCode.INTERNAL_ERROR, ex.getMessage()
                )
        );
    }

    private HttpStatus toHttpStatus(StateCode code) {
        return switch (code) {
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case CONFLICT -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private String safeToString(String field, Object v) {
        if (v == null) return "null";
        String s = String.valueOf(v);
        String lower = field.toLowerCase();
        if (lower.contains("password") || lower.contains("secret") || lower.contains("token")) return "****";
        if (s.length() > 128) s = s.substring(0, 125) + "...";
        return s;
    }
}
