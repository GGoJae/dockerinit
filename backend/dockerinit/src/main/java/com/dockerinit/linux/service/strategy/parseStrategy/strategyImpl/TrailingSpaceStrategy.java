package com.dockerinit.linux.service.strategy.parseStrategy.strategyImpl;

import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.service.strategy.parseStrategy.ParseStrategy;
import com.dockerinit.linux.util.ShellTokenizer;
import com.dockerinit.linux.model.AcPhase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrailingSpaceStrategy implements ParseStrategy {
    @Override
    public boolean matches(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        return cursor == line.length() && line.endsWith(" ");
    }

    @Override
    public ParseCtx apply(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        String baseCommand = getBaseCommand(tokens);
        return new ParseCtx(AcPhase.OPTION, baseCommand, "", null);
    }

    private String getBaseCommand(List<ShellTokenizer.Token> tokens) {
        return tokens.get(0).text();
    }

}
