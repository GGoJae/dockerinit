package com.dockerinit.linux.application.autocomplete.strategy.autocompleteStrategies;

import com.dockerinit.linux.application.autocomplete.parser.AutocompleteLineParser;
import com.dockerinit.linux.application.autocomplete.suggester.AutocompleteSuggester;

public interface AutocompleteCommandStrategy extends AutocompleteLineParser, AutocompleteSuggester {
    boolean supports(String command);
}
