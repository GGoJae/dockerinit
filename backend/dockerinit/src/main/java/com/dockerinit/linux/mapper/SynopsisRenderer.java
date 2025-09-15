package com.dockerinit.linux.mapper;

import com.dockerinit.linux.domain.syntax.Synopsis;
import com.dockerinit.linux.domain.syntax.SynopsisPattern;
import com.dockerinit.linux.domain.syntax.TokenDescriptor;
import com.dockerinit.linux.domain.syntax.TokenType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SynopsisRenderer {
    public static String renderOneLine(Synopsis syn) {
        if (syn == null) return null;
        // 첫 패턴만 한 줄로
        if (syn.patterns() == null || syn.patterns().isEmpty()) return null;
        return synToLine(syn.patterns().get(0));
    }

    private static String synToLine(SynopsisPattern p) {
        return p == null ? null : pTokensToLine(p.tokens());
    }

    private static String pTokensToLine(List<TokenDescriptor> tokens) {
        if (tokens == null || tokens.isEmpty()) return null;
        return tokens.stream()
                .map(SynopsisRenderer::renderToken)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private static String renderToken(TokenDescriptor td) {
        if (td == null) return null;
        String body;
        if (td.tokenType() == TokenType.ARGUMENT) {
            // ARG은 <NAME> 형태로
            body = "<" + td.literal() + ">";
        } else {
            body = td.literal();
        }
        if (body == null || body.isBlank()) return null;

        if (td.repeat()) body = body + "...";
        if (td.optional()) body = "[" + body + "]";
        return body;
    }

}
