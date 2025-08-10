package com.dockerinit.linux.domain.syntax;

public record TokenDescriptor(
        TokenType tokenType,    //   타입
        boolean repeat,     //      옵션 반복 가능 여부
        boolean optional,   //      선택적 토큰 여부
        String description  //      설명
) {}
