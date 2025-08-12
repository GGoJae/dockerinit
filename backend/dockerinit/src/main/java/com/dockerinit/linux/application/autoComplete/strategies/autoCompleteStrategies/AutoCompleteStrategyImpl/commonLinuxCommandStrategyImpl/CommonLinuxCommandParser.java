package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteStrategyImpl.commonLinuxCommandStrategyImpl;

import com.dockerinit.linux.application.autoComplete.model.CommandView;
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

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonLinuxCommandParser implements AutoCompleteLineParser {

    private final LinuxCommandRepository repository;

    @Override
    public ParseResult parse(String line, int cursor, List<ShellTokenizer.Token> tokens) {

        String baseCommand = tokens.isEmpty() ? "" : tokens.get(0).text();

        Optional<LinuxCommand> cmdOpt = baseCommand.isBlank() ? Optional.<LinuxCommand>empty() : repository.findByCommand(baseCommand);

        int idx = ShellTokenizer.indexOfTokenAtCursor(cursor, tokens);
        String currentTokenText = (idx >= 0 && idx < tokens.size()) ? tokens.get(idx).text() : "";

        int position = Math.max(0, idx - 1);
        String prevFlag = computePrevFlag(tokens, idx, currentTokenText);

        if (cmdOpt.isEmpty()) {
            List<ExpectedToken> expected = new ArrayList<>();
            if (currentTokenText.isEmpty()) {
                expected.add(new ExpectedToken(TokenType.COMMAND, 0, 1.0, Map.of()));
                if (currentTokenText.startsWith("-")) {
                    expected.add(new ExpectedToken(TokenType.OPTION, 1, 0.9, Map.of()));
                }
            } else {
                expected.add(new ExpectedToken(TokenType.COMMAND,10, 0.5, Map.of()));
            }
            return new ParseResult(
                    line, cursor, baseCommand, currentTokenText, idx,
                    prevFlag, null, expected, position);
        }

        CommandView command = CommandView.of(cmdOpt.get());

        Map<String, Option> options = command.options();

        List<TokenType> types = command.synopsis().expectedTypeAt(position);

        List<ExpectedToken> expected = getExpectedTokens(types, currentTokenText, prevFlag, options);

        return new ParseResult(
                line, cursor, baseCommand, currentTokenText, idx,
                prevFlag, command, expected, position
        );
    }

    private static List<ExpectedToken> getExpectedTokens(List<TokenType> types, String currentTokenText, String prevFlag, Map<String, Option> options) {
        List<ExpectedToken> expected = new ArrayList<>();

        int priority = 10;
        for (TokenType t : types) {
            expected.add(new ExpectedToken(t, priority++, 1.0, Map.of()));
        }

        if (!prevFlag.isBlank() && options.containsKey(prevFlag) && options.get(prevFlag).argRequired()) {
            expected.add(new ExpectedToken(TokenType.ARGUMENT, -3, 1.2, Map.of("prevFlag", prevFlag)));
        }

        if (currentTokenText.startsWith("-")) {
            expected.add(new ExpectedToken(TokenType.OPTION, -2, 1.1, Map.of()));
        }
        return expected;
    }

    private String computePrevFlag(List<ShellTokenizer.Token> tokens, int idx, String cur) {
        // 현재 토큰이 옵션이면 prevFlag는 비어있어야 함
        if (cur != null && cur.startsWith("-")) return "";
        for (int i = idx - 1; i >= 0; i--) {
            var t = tokens.get(i).text();
            if (t.startsWith("-")) return t;
            if (t.equals("|") || t.equals(">") || t.equals("<<") || t.equals("<")) {
                break;
            }
        }
        return "";
    }

}
