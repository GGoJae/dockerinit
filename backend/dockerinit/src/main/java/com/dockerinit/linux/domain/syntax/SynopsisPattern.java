package com.dockerinit.linux.domain.syntax;

import com.dockerinit.global.exception.InvalidInputCustomException;

import java.util.List;
import java.util.Map;

public record SynopsisPattern(List<TokenDescriptor> tokens) {
    public SynopsisPattern {
        tokens = List.copyOf(tokens);

        validateToken(tokens);
    }

    public TokenDescriptor at(int index) {
        return tokens.get(index);
    }

    public int size() {
        return tokens.size();
    }

    public TokenType expectedTypeAt(int position) {
        return position < tokens.size() ? tokens.get(position).tokenType() : null;
    }

    private void validateToken(List<TokenDescriptor> tokens) {
        if (tokens.isEmpty()) {
            throw new InvalidInputCustomException("synopsis pattern 이 비어있음", Map.of("tokens", tokens));
        }
        if (tokens.stream().anyMatch(td -> td.tokenType() == TokenType.COMMAND)) {
            throw new InvalidInputCustomException("pattern에 COMMAND 금지", Map.of("tokens", tokens));
        }

        // TODO 토큰 추가 검증 하기
    }
}
