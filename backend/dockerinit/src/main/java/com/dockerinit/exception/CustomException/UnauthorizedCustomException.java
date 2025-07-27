package com.dockerinit.exception.CustomException;

import com.dockerinit.dto.apiResponse.StateCode;

public class UnauthorizedCustomException extends CustomApiException{

    public UnauthorizedCustomException(String message) {
        super(message, StateCode.UNAUTHORIZED, null);
    }
}
