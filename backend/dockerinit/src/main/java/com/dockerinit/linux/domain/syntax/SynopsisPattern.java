package com.dockerinit.linux.domain.syntax;

import com.dockerinit.global.validation.ValidationCollector;

import java.util.List;
import java.util.Optional;

public record SynopsisPattern(List<TokenDescriptor> tokens) {
    public SynopsisPattern {
        ValidationCollector.create()
                .required("tokens", tokens, "tokens 이 비어있음")
                .noNullElements("tokens", tokens, "tokens 안에 null 존재")
                .forEachRejectIf("tokens", tokens,
                        td -> td.tokenType() == TokenType.COMMAND,
                        "pattern에 COMMAND 금지")
                .throwIfInvalid();

        tokens = List.copyOf(tokens);
    }

    public TokenDescriptor at(int index) {
        return tokens.get(index);
    }

    public int size() {
        return tokens.size();
    }

    public Optional<TokenType> expectedTypeAt(int position) {
        return position < tokens.size() ? Optional.of(tokens.get(position).tokenType()) : Optional.empty();
    }

    public static SynopsisPattern ofLiterals(List<String> literals) {
        List<TokenDescriptor> toks = literals.stream().map(TokenDescriptor::literal).toList();
        return new SynopsisPattern(toks);
    }

}
