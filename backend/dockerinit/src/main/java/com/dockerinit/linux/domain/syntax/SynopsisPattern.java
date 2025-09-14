package com.dockerinit.linux.domain.syntax;

import com.dockerinit.global.validation.ValidationCollector;

import java.util.List;

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
        ValidationCollector.create()
                .required("tokens", tokens, "tokens 이 비어있음")
                .forEachRejectIf("tokens", tokens,
                        td -> td.tokenType() == TokenType.COMMAND,
                        "pattern에 COMMAND 금지")
                .throwIfInvalid();

        // TODO 토큰 추가 검증 하기
    }
}
