package com.dockerinit.service.linux.strategy.parseStrategy.parseStrategyImpl;

import com.dockerinit.service.linux.ParseCtx;
import com.dockerinit.service.linux.strategy.parseStrategy.ParseStrategy;
import com.dockerinit.util.ShellTokenizer;
import com.dockerinit.vo.Linux.AcPhase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OnlyCommandStrategy implements ParseStrategy {
    @Override
    public boolean matches(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        ShellTokenizer.Token curToken = tokens.get(tokens.size() - 1);
        return tokens.size() == 1 && cursor <= curToken.end();
    }

    @Override
    public ParseCtx apply(String line, int cursor, List<ShellTokenizer.Token> tokens) {

        ShellTokenizer.Token currentToken = ShellTokenizer.currentToken(cursor, tokens);
        String cursorText = currentToken.text();

        return new ParseCtx(AcPhase.COMMAND, "", cursorText, null);
    }

}
