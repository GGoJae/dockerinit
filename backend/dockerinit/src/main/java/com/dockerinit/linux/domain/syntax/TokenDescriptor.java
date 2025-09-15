package com.dockerinit.linux.domain.syntax;

public record TokenDescriptor(
        TokenType tokenType,    //   타입
        String literal,
        boolean repeat,     //      옵션 반복 가능 여부
        boolean optional,   //      선택적 토큰 여부
        String description  //      설명
) {
    public static TokenDescriptor literal(String s) {
        return new TokenDescriptor(TokenType.LITERAL, s, false, false, null);
    }

    public static TokenDescriptor flag(String s) {
        return new TokenDescriptor(TokenType.FLAG, s, false, false, null);
    }

    public static TokenDescriptor argument(String name) {
        return new TokenDescriptor(TokenType.ARGUMENT, name, false, false, null);
    }

    public static TokenDescriptor of(TokenType type, boolean repeat, boolean optional, String description) {
        return new TokenDescriptor(type, null, repeat, optional, description);
    }

    public TokenDescriptor withRepeat(boolean v)   { return new TokenDescriptor(tokenType, literal, v, optional, description); }
    public TokenDescriptor withOptional(boolean v) { return new TokenDescriptor(tokenType, literal, repeat, v, description); }
    public TokenDescriptor withDesc(String d)      { return new TokenDescriptor(tokenType, literal, repeat, optional, d); }
}
