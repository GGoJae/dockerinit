package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;
import lombok.Getter;

@Getter
public class CustomApiException extends RuntimeException{
    private final StateCode errorCode;
    private final Object data;

    public CustomApiException(String message, StateCode errorCode, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }

    public CustomApiException(String message, Throwable cause, StateCode errorCode, Object data) {
        super(message, cause);
        this.errorCode = errorCode;
        this.data = data;
    }
}
