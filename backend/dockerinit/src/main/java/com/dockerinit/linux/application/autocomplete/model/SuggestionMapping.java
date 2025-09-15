package com.dockerinit.linux.application.autocomplete.model;

import com.dockerinit.linux.domain.syntax.TokenType;
import com.dockerinit.linux.dto.response.autocompleteV1.SuggestionType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SuggestionMapping {

    public static Optional<SuggestionType> fromTokenType(TokenType type) {
        return switch (type) {
            case OPTION, FLAG -> Optional.of(SuggestionType.OPTION);
            case ARGUMENT, VALUE -> Optional.of(SuggestionType.ARGUMENT);
            case FILE, DIRECTORY, PATH, SOURCE, DESTINATION -> Optional.of(SuggestionType.TARGET);
            case COMMAND -> Optional.of(SuggestionType.COMMAND);
            case LITERAL -> Optional.empty();
        };
    }
}
