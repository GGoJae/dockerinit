package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteStratyImpl.commonLinuxCommandStrategies;

import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteCommandStrategy;
import com.dockerinit.linux.dto.response.v2.SuggestionV2;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonLinuxCommandStrategy implements AutoCompleteCommandStrategy {

    private final CommonLinuxCommandParser parser;
    private final CommonLinuxCommandSuggester suggester;

    @Override
    public boolean supports(String command) {
        return true; // TODO 일단 모든 명령어 처리, 이후 Common Linux Command 리스트 만들어서 contains 로 로직 변경
    }

    @Override
    public ParseResult parse(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        return parser.parse(line, cursor, tokens);
    }


    @Override
    public List<SuggestionV2> suggest(ParseResult result, List<ShellTokenizer.Token> tokens) {
        return suggester.suggest(result, tokens);
    }
}
