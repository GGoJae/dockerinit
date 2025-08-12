package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies;

import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.dto.response.v2.Suggestion;
import com.dockerinit.linux.util.ShellTokenizer;

import java.util.List;

public interface AutoCompleteSuggester {
    List<Suggestion> suggest(ParseResult result, List<ShellTokenizer.Token> tokens);
}
