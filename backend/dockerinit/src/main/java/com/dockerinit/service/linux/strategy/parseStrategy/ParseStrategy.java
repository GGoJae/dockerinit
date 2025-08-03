package com.dockerinit.service.linux.strategy.parseStrategy;

import com.dockerinit.service.linux.ParseCtx;
import com.dockerinit.util.ShellTokenizer;

import java.util.List;

public interface ParseStrategy {

    boolean matches(String line, int cursor, List<ShellTokenizer.Token> tokens);

    ParseCtx apply(String line, int cursor, List<ShellTokenizer.Token> tokens);

}
