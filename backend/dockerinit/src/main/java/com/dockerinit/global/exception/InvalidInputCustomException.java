package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;

import java.util.Map;

public class InvalidInputCustomException extends CustomApiException{

    public InvalidInputCustomException(String message) {
        super(message, StateCode.INVALID_INPUT, null);
    }
    public InvalidInputCustomException(String message, Map<String, Object> data) {
        super(message, StateCode.INVALID_INPUT, data);
    }
}
