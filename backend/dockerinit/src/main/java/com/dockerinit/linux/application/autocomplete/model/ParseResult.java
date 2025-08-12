package com.dockerinit.linux.application.autocomplete.model;

import com.dockerinit.linux.domain.syntax.Option;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

public record ParseResult(
        String line,
        int cursor,
        String baseCommand,
        String currentToken,
        int tokenIndex,
        String prevFlag,
        @Nullable CommandView command,
        List<ExpectedToken> expected,
        int position    //  시놉시스 위치
) {

    public ParseResult {
        line = (line == null) ? "" : line;
        baseCommand = (baseCommand == null) ? "" : baseCommand;
        currentToken = (currentToken == null) ? "" : currentToken;
        prevFlag = (prevFlag == null) ? "" : prevFlag;
        expected = (expected == null) ? List.of() : List.copyOf(expected);
        tokenIndex = Math.max(-1, tokenIndex);
        position = Math.max(0, position);
    }

    public Map<String, Option> optionOrEmpty() {
        return command == null ? Map.of() : command.options();
    }
}
