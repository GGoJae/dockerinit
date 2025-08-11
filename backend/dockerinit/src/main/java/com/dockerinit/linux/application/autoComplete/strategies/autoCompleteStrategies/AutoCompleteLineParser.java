package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies;

import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.util.ShellTokenizer;

import java.util.List;

public interface AutoCompleteLineParser {
    ParseResult parse(String line, int cursor, List<ShellTokenizer.Token> tokens);
}
