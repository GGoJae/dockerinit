package com.dockerinit.linux.application.autocomplete.strategy.autocompleteStrategies.impl;

import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.shared.parser.CommonLinuxCommandParser;
import com.dockerinit.linux.application.autocomplete.strategy.autocompleteStrategies.AutocompleteStrategy;
import com.dockerinit.linux.application.autocomplete.suggester.CommonLinuxCommandSuggester;
import com.dockerinit.linux.application.shared.model.ModuleType;
import com.dockerinit.linux.dto.response.autocompleteV1.Suggestion;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonLinuxAutocompleteStrategy implements AutocompleteStrategy {

    private final CommonLinuxCommandParser parser;
    private final CommonLinuxCommandSuggester suggester;


    @Override
    public ModuleType moduleType() {
        return ModuleType.LINUX;
    }

    @Override
    public ParseResult parse(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        return parser.parse(line, cursor, tokens);
    }


    @Override
    public List<Suggestion> suggest(ParseResult result, List<ShellTokenizer.Token> tokens) {
        return suggester.suggest(result, tokens);
    }
}
