package com.dockerinit.linux.service.strategy.linuxCommandStrategy.linuxCommandStrategyImpl.commonLinuxCommandStrategy;

import com.dockerinit.linux.domain.LinuxCommand;
import com.dockerinit.linux.model.AcPhase;
import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonLinuxParser {

    private final LinuxCommandRepository repository;

    public ParseCtx parse(String line, int cursor, List<ShellTokenizer.Token> tokens) {

        // 라인이 비어있으면 현재 페이즈는 커맨드
        if (tokens.isEmpty()) {
            return new ParseCtx(AcPhase.COMMAND, "", "", "");
        }

        String baseCommand = tokens.get(0).text();

        ShellTokenizer.Token current = ShellTokenizer.currentToken(cursor, tokens);
        if (current == null && !tokens.isEmpty()) {
            current = tokens.get(tokens.size() - 1);
        }
        String currentToken = current == null ? "" : current.text();

        String prevFlag = findPrevFlag(tokens, current);
        AcPhase phase = decidePhase(tokens, current, prevFlag, baseCommand);

        return new ParseCtx(phase, baseCommand, currentToken, prevFlag );
    }



    private String findPrevFlag(List<ShellTokenizer.Token> tokens, ShellTokenizer.Token current) {
        int idx = tokens.indexOf(current);

        if (idx == -1 && current != null) {
            for (int i = tokens.size() - 1; i >= 0; i--) {
                String t = tokens.get(i).text();
                if (t.startsWith("-")) return t;
            }
            return "";
        }

        if (current != null && current.text().startsWith("-")) {
            return current.text();
        }

        // 직전 토큰들 중에서 플래그 탐색
        for (int i = idx - 1; i >= 0; i--) {
            String t = tokens.get(i).text();
            if (t.startsWith("-")) {
                return t;
            }
        }

        return "";
    }


    /**
     * Phase 판단 로직
     */
    private AcPhase decidePhase(List<ShellTokenizer.Token> tokens, ShellTokenizer.Token current, String prevFlag, String baseCommand) {
        if (tokens.size() == 1) return AcPhase.COMMAND;

        String currentText = current == null ? "" : current.text();

        if (currentText.isEmpty()) {
            if (!prevFlag.isEmpty()) {
                boolean argRequired = optionRequiresArg(baseCommand, prevFlag);
                return argRequired ? AcPhase.ARGUMENT : AcPhase.OPTION_OR_ARGUMENT;
            }
            return AcPhase.TARGET;
        }

        if (currentText.startsWith("-")) return AcPhase.OPTION;

        if (!prevFlag.isEmpty()) {
            boolean argRequired = optionRequiresArg(baseCommand, prevFlag);
            return argRequired ? AcPhase.ARGUMENT : AcPhase.OPTION_OR_ARGUMENT;
        }

        return AcPhase.TARGET;
    }

    private boolean optionRequiresArg(String command, String flag) {
        Optional<LinuxCommand> commandOptional = repository.findByCommand(command);
        log.info("커맨드 정보 {}", commandOptional.orElseGet(() -> null));
        return
                commandOptional.map(cmd -> Optional.ofNullable(cmd.getOptions().getOrDefault(flag, null)))
                .flatMap(opt -> opt.map(LinuxCommand.OptionInfo::argRequired))
                .orElse(false);
    }

}
