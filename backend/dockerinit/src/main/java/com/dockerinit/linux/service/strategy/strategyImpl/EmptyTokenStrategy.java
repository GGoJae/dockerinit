package com.dockerinit.linux.service.strategy.strategyImpl;

import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.service.strategy.ParseStrategy;
import com.dockerinit.linux.util.ShellTokenizer;
import com.dockerinit.linux.model.AcPhase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmptyTokenStrategy implements ParseStrategy {

    @Override
    public boolean matches(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        return tokens.isEmpty();
    }

    @Override
    public ParseCtx apply(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        return new ParseCtx(AcPhase.COMMAND, "", "", null);
    }

}
