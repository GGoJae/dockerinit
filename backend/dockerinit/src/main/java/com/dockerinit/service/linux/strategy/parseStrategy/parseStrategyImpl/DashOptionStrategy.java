package com.dockerinit.service.linux.strategy.parseStrategy.parseStrategyImpl;

import com.dockerinit.service.linux.ParseCtx;
import com.dockerinit.service.linux.strategy.parseStrategy.ParseStrategy;
import com.dockerinit.util.ShellTokenizer;
import com.dockerinit.vo.Linux.AcPhase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DashOptionStrategy implements ParseStrategy {
    @Override
    public boolean matches(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        ShellTokenizer.Token cursorToken = ShellTokenizer.currentToken(cursor, tokens);
        String cursorText = cursorToken.text();

        return cursorText.startsWith("-");
    }

    @Override
    public ParseCtx apply(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        String currentText = ShellTokenizer.currentToken(cursor, tokens).text();
        String baseCommand = getBaseCommand(tokens);
        return new ParseCtx(AcPhase.OPTION, baseCommand, currentText, null);
    }

    private String getBaseCommand(List<ShellTokenizer.Token> tokens) {
        return tokens.get(0).text();
    }

}
