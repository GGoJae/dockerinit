package com.dockerinit.linux.application.autoComplete;

import com.dockerinit.linux.domain.syntax.TokenType;
import com.dockerinit.linux.dto.response.SuggestionType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SuggestionMapping {

    public static SuggestionType fromTokenType(TokenType type) {
        return switch (type) {
            case OPTION, FLAG -> SuggestionType.OPTION;
            case ARGUMENT, VALUE -> SuggestionType.ARGUMENT;
            case FILE, DIRECTORY, PATH, SOURCE, DESTINATION -> SuggestionType.TARGET;
            case COMMAND -> SuggestionType.COMMAND;
        };
    }
}
