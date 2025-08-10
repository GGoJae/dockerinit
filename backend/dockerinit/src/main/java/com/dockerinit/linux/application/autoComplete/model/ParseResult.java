package com.dockerinit.linux.application.autoComplete.model;

import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.TokenType;

import java.util.List;
import java.util.Map;

public record ParseResult(
        String line,
        int cursor,
        String baseCommand,
        String currentToken,
        int tokenIndex,
        String prevFlag,
        LinuxCommand command,
        Map<String, Option> options,
        List<ExpectedToken> expected,
        int position    //  시놉시스 위치
) {
    public boolean pathLikePrefix() {
        if (currentToken == null) return false;
        return currentToken.startsWith("/") || currentToken.startsWith("./") || currentToken.startsWith("../");
    }

    public boolean looksLikeOption() {
        return currentToken != null && currentToken.startsWith("-");
    }

    public List<TokenType> expectedTypesOnly() {
        return expected.stream()
                .sorted().map(ExpectedToken::type)
                .toList();
    }
}
