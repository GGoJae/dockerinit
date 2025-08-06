package com.dockerinit.linux.service.strategy.parseStrategy;

import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.util.ShellTokenizer;

import java.util.List;

public interface ParseStrategy {

    boolean matches(String line, int cursor, List<ShellTokenizer.Token> tokens);

    ParseCtx apply(String line, int cursor, List<ShellTokenizer.Token> tokens);

}
