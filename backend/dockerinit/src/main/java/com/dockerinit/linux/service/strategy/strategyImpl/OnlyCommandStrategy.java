package com.dockerinit.linux.service.strategy.strategyImpl;

import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.service.strategy.ParseStrategy;
import com.dockerinit.linux.util.ShellTokenizer;
import com.dockerinit.linux.model.AcPhase;
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
