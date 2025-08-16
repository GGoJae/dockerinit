package com.dockerinit.linux.application.autocomplete.strategy.autocompleteStrategies;

import com.dockerinit.linux.application.shared.parser.CommandLineParser;
import com.dockerinit.linux.application.autocomplete.suggester.AutocompleteSuggester;
import com.dockerinit.linux.application.shared.model.ModuleType;

public interface AutocompleteStrategy extends CommandLineParser, AutocompleteSuggester {
    ModuleType moduleType();
}
