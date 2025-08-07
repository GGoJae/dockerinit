package com.dockerinit.linux.service.strategy.linuxCommandStrategy.linuxCommandStrategyImpl.commonLinuxCommandStrategy;

import com.dockerinit.linux.domain.LinuxCommand;
import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.model.Suggestion;
import com.dockerinit.linux.model.SuggestionType;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.service.strategy.linuxCommandStrategy.LinuxCommandStrategy;
import com.dockerinit.linux.util.RedisKeys;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonLinuxCommandStrategy implements LinuxCommandStrategy {

    private final CommonLinuxCommandParser parser;
    private final CommonLinuxCommandSuggester suggester;

    @Override
    public boolean supports(String command) {
        return true; // TODO 일단 모든 명령어 처리, 이후 Common Linux Command 리스트 만들어서 contains 로 로직 변경
    }

    @Override
    public ParseCtx parse(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        return parser.parse(line, cursor, tokens);
    }

    @Override
    public List<Suggestion> suggest(ParseCtx ctx) {
        return suggester.suggest(ctx);
    }

}
