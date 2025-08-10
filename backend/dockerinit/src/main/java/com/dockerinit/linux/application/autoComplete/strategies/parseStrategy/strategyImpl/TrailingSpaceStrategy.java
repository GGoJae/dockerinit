package com.dockerinit.linux.application.autoComplete.strategies.parseStrategy.strategyImpl;

import com.dockerinit.linux.dto.vo.ParseCtx;
import com.dockerinit.linux.application.autoComplete.strategies.parseStrategy.ParseStrategy;
import com.dockerinit.linux.util.ShellTokenizer;
import com.dockerinit.linux.application.autoComplete.model.AcPhase;
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
//        return new ParseCtx(AcPhase.OPTION, baseCommand, "", null);
        return null;    // TODO 아마 이 파서전략은 지워질 예정
    }

    private String getBaseCommand(List<ShellTokenizer.Token> tokens) {
        return tokens.get(0).text();
    }

}
