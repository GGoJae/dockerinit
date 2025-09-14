package com.dockerinit.global.exception;

import com.dockerinit.global.response.StateCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.function.Function;
import java.util.function.Supplier;

public class NotFoundCustomException extends CustomApiException{
    private static final Function<String, String> DEFAULT_MESSAGE_FUNCTION =
            (resource) -> "해당 " + resource + "를 찾을 수 없습니다.";

    public NotFoundCustomException(String message) {
        super(message, StateCode.NOT_FOUND, null);
    }

    public NotFoundCustomException(String message, String resource ,String field, Object value) {
        super(message, StateCode.NOT_FOUND, NotFoundContext.of(resource, field, value));
    }

    public static NotFoundCustomException of(String message, String resource, String field, Object value) {
        return new NotFoundCustomException(message, resource, field, value);
    }

    public static NotFoundCustomException of(String resource, String field, Object value) {
        return of(DEFAULT_MESSAGE_FUNCTION.apply(resource), resource, field, value);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record NotFoundContext(String resource, String field, Object value) {

        private static NotFoundContext of(String resource, String field, Object value) {
            return new NotFoundContext(resource, field, value);
        }
    }
}
