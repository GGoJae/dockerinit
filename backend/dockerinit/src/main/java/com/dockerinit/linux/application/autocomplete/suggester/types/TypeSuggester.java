package com.dockerinit.linux.application.autocomplete.suggester.types;

import com.dockerinit.linux.application.autocomplete.model.ExpectedToken;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.dto.response.common.SuggestionType;
import com.dockerinit.linux.dto.response.autocompleteV1.Suggestion;
import com.dockerinit.linux.application.autocomplete.replace.Replace;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;

import java.util.List;

public interface TypeSuggester {
    SuggestionType type();
    List<Suggestion> collect(
            ParseResult ctx,
            List<ShellTokenizer.Token> tokens,
            ExpectedToken slot,
            Replace.Range range,
            int limit
    );
}
