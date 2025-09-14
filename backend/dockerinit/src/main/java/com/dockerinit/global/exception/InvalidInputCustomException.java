package com.dockerinit.global.exception;

import com.dockerinit.global.exception.model.ErrorContent;
import com.dockerinit.global.response.StateCode;

import java.util.List;
import java.util.Map;

public class InvalidInputCustomException extends CustomApiException {

    public InvalidInputCustomException(String message, Map<String, List<ErrorContent>> data) {
        super(message, StateCode.INVALID_INPUT, data);
    }

}
