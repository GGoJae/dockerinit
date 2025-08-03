package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;

public class UnauthorizedCustomException extends CustomApiException{

    public UnauthorizedCustomException(String message) {
        super(message, StateCode.UNAUTHORIZED, null);
    }
}
