package com.dockerinit.linux.application.shared.parser;

import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;

import java.util.List;

public interface CommandLineParser {
    ParseResult parse(String line, int cursor, List<ShellTokenizer.Token> tokens);
}
