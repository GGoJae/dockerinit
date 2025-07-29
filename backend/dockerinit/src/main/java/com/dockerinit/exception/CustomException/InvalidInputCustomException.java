package com.dockerinit.exception.CustomException;

import com.dockerinit.dto.apiResponse.StateCode;

import java.util.Map;

public class InvalidInputCustomException extends CustomApiException{
    public InvalidInputCustomException(String message, Map<String, Object> data) {
        super(message, StateCode.INVALID_INPUT, data);
    }
}
