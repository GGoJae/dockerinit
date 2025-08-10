package com.dockerinit.linux.application.autoComplete.strategies.parseStrategy;

import com.dockerinit.linux.dto.vo.ParseCtx;
import com.dockerinit.linux.util.ShellTokenizer;

import java.util.List;

public interface ParseStrategy {

    boolean matches(String line, int cursor, List<ShellTokenizer.Token> tokens);

    ParseCtx apply(String line, int cursor, List<ShellTokenizer.Token> tokens);

}
