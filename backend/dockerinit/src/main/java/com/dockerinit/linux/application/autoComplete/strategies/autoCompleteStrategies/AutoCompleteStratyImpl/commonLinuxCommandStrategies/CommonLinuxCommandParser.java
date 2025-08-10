package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteStratyImpl.commonLinuxCommandStrategies;

import com.dockerinit.linux.application.autoComplete.model.AcPhase;
import com.dockerinit.linux.application.autoComplete.model.ExpectedToken;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteLineParser;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.TokenType;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonLinuxCommandParser implements AutoCompleteLineParser {

    private final LinuxCommandRepository repository;

    @Override
    public ParseResult parse(String line, int cursor, List<ShellTokenizer.Token> tokens) {

        // 라인이 비어있으면 현재 페이즈는 커맨드
//        if (tokens.isEmpty()) {
//            return new ParseCtx(AcPhase.COMMAND, "", "", "");
//        }
//
//        String baseCommand = tokens.get(0).text();
//
//        ShellTokenizer.Token current = ShellTokenizer.currentToken(cursor, tokens);
//        if (current == null && !tokens.isEmpty()) {
//            current = tokens.get(tokens.size() - 1);
//        }
//        String currentToken = current == null ? "" : current.text();
//
//        String prevFlag = findPrevFlag(tokens, current);
//        AcPhase phase = decidePhase(tokens, current, prevFlag, baseCommand);
//
//        return new ParseCtx(phase, baseCommand, currentToken, prevFlag );

        String baseCommand = tokens.isEmpty() ? "" : tokens.get(0).text();
        Optional<LinuxCommand> cmdOpt = baseCommand.isBlank() ? Optional.<LinuxCommand>empty() : repository.findByCommand(baseCommand);

        int idx = ShellTokenizer.indexOfTokenAtCursor(cursor, tokens);
        String currentTokenText = (idx >= 0 && idx < tokens.size()) ? tokens.get(idx).text() : "";

        int position = Math.max(0, idx - 1);
        String prevFlag = computePrevFlag(tokens, idx, currentTokenText);

        LinuxCommand command = cmdOpt.orElseGet(() -> null);
        Map<String, Option> options = Optional.ofNullable(command)
                .map(cmd -> cmd.getOptions())
                .orElseGet(() -> Map.of());

        List<TokenType> types = Optional.ofNullable(command)
                .map(cmd -> cmd.getSynopsis())
                .map(syn -> syn.expectedTypeAt(position))
                .orElseGet(() -> List.of());

        List<ExpectedToken> expected = new ArrayList<>();
        int priority = 10;
        for (TokenType t : types) {
            expected.add(new ExpectedToken(t, priority++, 1.0, Map.of()));
        }
        if (currentTokenText.startsWith("-")) {
            expected.add(new ExpectedToken(TokenType.OPTION, 0, 1.1, Map.of()));
        }
        if (!prevFlag.isBlank() && options.containsKey(prevFlag) && options.get(prevFlag).argRequired()) {
            expected.add(new ExpectedToken(TokenType.ARGUMENT, -1, 1.2, Map.of()));
        }

        return new ParseResult(
                line, cursor, baseCommand, currentTokenText, idx,
                prevFlag, command, options, expected, position
        );
    }

// TODO computePrevFlag 확장되면 지워도 될듯
//    private String findPrevFlag(List<ShellTokenizer.Token> tokens, ShellTokenizer.Token current) {
//        int idx = tokens.indexOf(current);
//
//        if (idx == -1 && current != null) {
//            for (int i = tokens.size() - 1; i >= 0; i--) {
//                String t = tokens.get(i).text();
//                if (t.startsWith("-")) return t;
//            }
//            return "";
//        }
//
//        if (current != null && current.text().startsWith("-")) {
//            return current.text();
//        }
//
//        // 직전 토큰들 중에서 플래그 탐색
//        for (int i = idx - 1; i >= 0; i--) {
//            String t = tokens.get(i).text();
//            if (t.startsWith("-")) {
//                return t;
//            }
//        }
//
//        return "";
//    }

    private String computePrevFlag(List<ShellTokenizer.Token> tokens, int idx, String cur) {
        // 현재 토큰이 옵션이면 prevFlag는 비어있어야 함
        if (cur != null && cur.startsWith("-")) return "";
        for (int i = idx - 1; i >= 0; i--) {
            var t = tokens.get(i).text();
            if (t.startsWith("-")) return t;
            // 파이프/리다이렉션 만나면 중단이 필요하면 여기서 break
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
                        .flatMap(opt -> opt.map(Option::argRequired))
                        .orElse(false);
    }

}
