package com.dockerinit.linux.service.strategy.linuxCommandStrategy;

import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.model.Suggestion;
import com.dockerinit.linux.util.ShellTokenizer;

import java.util.List;

public interface LinuxCommandStrategy {
    boolean supports(String command);
    ParseCtx parse(String line, int cursor, List<ShellTokenizer.Token> tokens);
    List<Suggestion> suggest(ParseCtx ctx);
}
