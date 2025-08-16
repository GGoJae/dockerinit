package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;

import java.util.Map;

public class BadRequestCustomException extends CustomApiException{
    public BadRequestCustomException(String message, Map<String, String> invalidFields) {
        super(message, StateCode.INVALID_INPUT, invalidFields);
    }
}
