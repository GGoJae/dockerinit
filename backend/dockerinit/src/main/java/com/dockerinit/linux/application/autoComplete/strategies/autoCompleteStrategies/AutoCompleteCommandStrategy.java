package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies;

public interface AutoCompleteCommandStrategy extends AutoCompleteLineParser, AutoCompleteSuggester {
    boolean supports(String command);
}
