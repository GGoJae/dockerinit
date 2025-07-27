package com.dockerinit.exception.CustomException;

import com.dockerinit.dto.apiResponse.StateCode;

public class ConflictCustomException extends CustomApiException{
    public ConflictCustomException(String message) {
        super(message, StateCode.CONFLICT, null);
    }
}
