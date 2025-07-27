package com.dockerinit.exception.CustomException;

import com.dockerinit.dto.apiResponse.StateCode;

public class NotFoundCustomException extends CustomApiException{
    public NotFoundCustomException(String message) {
        super(message, StateCode.NOT_FOUND, null);
    }
}
