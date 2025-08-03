package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;

public class InternalErrorCustomException extends CustomApiException {

    public InternalErrorCustomException(String message) {
        super(message, StateCode.INTERNAL_ERROR, null);
    }

    public InternalErrorCustomException(String message, Throwable cause) {
        super(message, cause,  StateCode.INTERNAL_ERROR, null);
    }
}
