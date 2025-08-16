package com.dockerinit.linux.domain.syntax;

public record Option(
        String argName,        // 인자 이름 (예: count)
        boolean argRequired,   // 옵션 뒤에 인자 필수 여부
        String typeHint,       // 인자 타입 힌트 (예: int, string)
        String defaultValue,   // 기본값 (nullable)
        String description     // 옵션 설명
) {}
