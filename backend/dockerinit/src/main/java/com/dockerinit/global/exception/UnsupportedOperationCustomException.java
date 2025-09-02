package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;

import java.util.Map;

public class UnsupportedOperationCustomException extends CustomApiException{
    public UnsupportedOperationCustomException(String message, Map<String, Object> bindingResult) {
        super(message, StateCode.UNSUPPORTED_OPERATION, bindingResult);
    }
}
