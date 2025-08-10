package com.dockerinit.linux.application.autoComplete.strategies.parseStrategy.strategyImpl;

import com.dockerinit.linux.dto.vo.ParseCtx;
import com.dockerinit.linux.application.autoComplete.strategies.parseStrategy.ParseStrategy;
import com.dockerinit.linux.util.ShellTokenizer;
import com.dockerinit.linux.application.autoComplete.model.AcPhase;
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

//        return new ParseCtx(AcPhase.COMMAND, "", cursorText, null);
        return null;    // TODO 아마 이 파서전략은 지워질 예정
    }

}
