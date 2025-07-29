package com.dockerinit.exception.CustomException;

import com.dockerinit.dto.apiResponse.StateCode;

public class RateLimitExceededException extends CustomApiException{
    public RateLimitExceededException(String message) {
        super(message, StateCode.CONFLICT, null);
    }
}
