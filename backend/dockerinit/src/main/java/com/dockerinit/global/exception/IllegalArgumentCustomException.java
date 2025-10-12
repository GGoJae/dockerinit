package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;

public class IllegalArgumentCustomException extends CustomApiException{
    public IllegalArgumentCustomException(String message) {
        super(message, StateCode.INVALID_INPUT, null);
    }

    public IllegalArgumentCustomException(String message, Object data) {
        super(message, StateCode.INVALID_INPUT, data);
    }
}
