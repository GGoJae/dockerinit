package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;

public class ConflictCustomException extends CustomApiException{
    public ConflictCustomException(String message) {
        super(message, StateCode.CONFLICT, null);
    }
}
