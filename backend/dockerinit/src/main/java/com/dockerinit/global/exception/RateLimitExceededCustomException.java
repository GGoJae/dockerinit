package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;

public class RateLimitExceededCustomException extends CustomApiException{
    public RateLimitExceededCustomException(String message) {
        super(message, StateCode.CONFLICT, null);
    }
}
