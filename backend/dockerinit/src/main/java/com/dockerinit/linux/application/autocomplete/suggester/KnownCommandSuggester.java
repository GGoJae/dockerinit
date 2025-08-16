package com.dockerinit.linux.application.autocomplete.suggester;

import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.dto.response.autocompleteV1.Suggestion;

import java.util.List;

public class KnownCommandSuggester implements AutocompleteSuggester {
    @Override
    public List<Suggestion> suggest(ParseResult result, List<ShellTokenizer.Token> tokens) {
        return List.of();
    }
}
