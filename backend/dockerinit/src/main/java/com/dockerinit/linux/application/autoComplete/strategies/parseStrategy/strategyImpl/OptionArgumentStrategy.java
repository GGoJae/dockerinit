package com.dockerinit.linux.application.autoComplete.strategies.parseStrategy.strategyImpl;

import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.dto.vo.ParseCtx;
import com.dockerinit.linux.application.autoComplete.strategies.parseStrategy.ParseStrategy;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OptionArgumentStrategy implements ParseStrategy {

    private final LinuxCommandRepository repository;

    @Override
    public boolean matches(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        String prevToken = getPrevToken(tokens);
        String baseCommand = getBaseCommand(tokens);

        return prevToken.startsWith("-") && optionRequiresArg(baseCommand, prevToken);
    }

    @Override
    public ParseCtx apply(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        String currentToken = ShellTokenizer.currentToken(cursor, tokens).text();
        String prevToken = getPrevToken(tokens);
        String baseCommand = getBaseCommand(tokens);

//        return new ParseCtx(AcPhase.ARGUMENT, baseCommand, currentToken, prevToken);
        return null;    // TODO 아마 이 파서전략은 지워질 예정
    }

    private String getBaseCommand(List<ShellTokenizer.Token> tokens) {
        return tokens.get(0).text();
    }

    private String getPrevToken(List<ShellTokenizer.Token> tokens) {
        return tokens.size() >= 2 ? tokens.get(tokens.size() - 2).text() : "";
    }

    private boolean optionRequiresArg(String cmd, String flag) {
        return repository.findByCommand(cmd)
                .map(c -> Optional.ofNullable(c.getOptions().get(flag)))
                .flatMap(o -> o.map(Option::argRequired))
                .orElseGet(() -> false);
    }

}
