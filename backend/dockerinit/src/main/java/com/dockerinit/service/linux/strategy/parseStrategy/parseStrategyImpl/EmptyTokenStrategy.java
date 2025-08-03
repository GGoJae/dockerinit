package com.dockerinit.service.linux.strategy.parseStrategy.parseStrategyImpl;

import com.dockerinit.service.linux.ParseCtx;
import com.dockerinit.service.linux.strategy.parseStrategy.ParseStrategy;
import com.dockerinit.util.ShellTokenizer;
import com.dockerinit.vo.Linux.AcPhase;
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
