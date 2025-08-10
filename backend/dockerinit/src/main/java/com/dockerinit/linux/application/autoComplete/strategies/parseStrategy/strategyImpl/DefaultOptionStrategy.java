package com.dockerinit.linux.application.autoComplete.strategies.parseStrategy.strategyImpl;

import com.dockerinit.linux.dto.vo.ParseCtx;
import com.dockerinit.linux.application.autoComplete.strategies.parseStrategy.ParseStrategy;
import com.dockerinit.linux.util.ShellTokenizer;
import com.dockerinit.linux.application.autoComplete.model.AcPhase;

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

//        return new ParseCtx(AcPhase.OPTION, baseCommand, currentToken, null);
        return null;    // TODO 아마 이 파서전략은 지워질 예정
    }

    private String getBaseCommand(List<ShellTokenizer.Token> tokens) {
        return tokens.get(0).text();
    }

}
