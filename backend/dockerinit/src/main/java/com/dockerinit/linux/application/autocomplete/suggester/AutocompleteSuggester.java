package com.dockerinit.linux.application.autocomplete.suggester;

import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.dto.response.autocompleteV1.Suggestion;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;

import java.util.List;

public interface AutocompleteSuggester {
    List<Suggestion> suggest(ParseResult result, List<ShellTokenizer.Token> tokens);
}
