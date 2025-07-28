package com.dockerinit.exception.CustomException;

import com.dockerinit.dto.apiResponse.StateCode;

public class InternalErrorCustomException extends CustomApiException {
    public InternalErrorCustomException(String message, Throwable cause) {
        super(message, cause,  StateCode.INTERNAL_ERROR, null);
    }
}
