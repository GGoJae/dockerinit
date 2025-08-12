package com.dockerinit.linux.application.autocomplete.parser;

import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.autocomplete.tokenizer.ShellTokenizer;

import java.util.List;

public interface AutocompleteLineParser {
    ParseResult parse(String line, int cursor, List<ShellTokenizer.Token> tokens);
}
