package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;

public class NotFoundCustomException extends CustomApiException{
    public NotFoundCustomException(String message) {
        super(message, StateCode.NOT_FOUND, null);
    }

    public NotFoundCustomException(String message, Object data) {
        super(message, StateCode.NOT_FOUND, data);
    }
}
