package com.dockerinit.linux.application.autoComplete.model;

import com.dockerinit.linux.domain.syntax.TokenType;

import java.util.Map;

public record ExpectedToken(
        TokenType type,          // FILE, DIRECTORY, ARGUMENT, OPTION, ...
        int priority,            // 낮을수록 우선
        double confidence,       // 0.0 ~ 1.0
        Map<String, Object> meta // prevFlag, optionMeta, pathHint 등 부가정보
) implements Comparable<ExpectedToken>{
    @Override
    public int compareTo(ExpectedToken o) {
        int p = Integer.compare(this.priority, o.priority);
        return p != 0 ? p : -Double.compare(this.confidence, o.confidence);
    }

    public static ExpectedToken of(TokenType type, int priority) {
        return new ExpectedToken(type, priority, 1.0, Map.of());
    }
}
