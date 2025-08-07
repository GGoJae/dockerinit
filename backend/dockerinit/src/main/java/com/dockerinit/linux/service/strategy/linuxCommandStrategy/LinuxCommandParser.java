package com.dockerinit.linux.service.strategy.linuxCommandStrategy;

import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.util.ShellTokenizer;

import java.util.List;

public interface LinuxCommandParser {
    ParseCtx parse(String line, int cursor, List<ShellTokenizer.Token> tokens);
}
