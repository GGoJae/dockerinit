package com.dockerinit.linux.application.autoComplete.strategies.parseStrategy.strategyImpl;

import com.dockerinit.linux.dto.vo.ParseCtx;
import com.dockerinit.linux.application.autoComplete.strategies.parseStrategy.ParseStrategy;
import com.dockerinit.linux.util.ShellTokenizer;
import com.dockerinit.linux.application.autoComplete.model.AcPhase;
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
//        return new ParseCtx(AcPhase.COMMAND, "", "", null);
        return null;    // TODO 아마 이 파서전략은 지워질 예정
    }

}
