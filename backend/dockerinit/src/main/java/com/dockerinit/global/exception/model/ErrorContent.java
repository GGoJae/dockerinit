package com.dockerinit.global.exception.model;

public record ErrorContent(
        String message,
        String subfield,
        Integer index,
        String rejectedValue
) {

    public static ErrorContent withoutMessage(String rejectedValue) {
        return of("공통 메세지 참고", null, null, rejectedValue);
    }

    public static ErrorContent of(String message, String rejectedValue) {
        return of(message, null, null, rejectedValue);
    }

    public static ErrorContent of(String message, String subfield, Integer index, String rejectedValue) {
        return new ErrorContent(message, subfield, index, rejectedValue);
    }

}
