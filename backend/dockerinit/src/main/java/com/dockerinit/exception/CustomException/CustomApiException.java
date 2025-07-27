package com.dockerinit.exception.CustomException;

import com.dockerinit.dto.apiResponse.StateCode;
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
}
