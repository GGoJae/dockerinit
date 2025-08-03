package com.dockerinit.linux.service.strategy.strategyImpl;

import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.service.strategy.ParseStrategy;
import com.dockerinit.linux.util.ShellTokenizer;
import com.dockerinit.linux.model.AcPhase;

import java.util.List;

/**
 * fallback strategy
 */

public class DefaultOptionStrategy implements ParseStrategy {
    @Override
    public boolean matches(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        return false;
    }

    @Override
    public ParseCtx apply(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        String baseCommand = getBaseCommand(tokens);
        String currentToken = ShellTokenizer.currentToken(cursor, tokens).text();

        return new ParseCtx(AcPhase.OPTION, baseCommand, currentToken, null);
    }

    private String getBaseCommand(List<ShellTokenizer.Token> tokens) {
        return tokens.get(0).text();
    }

}
