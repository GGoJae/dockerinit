package com.dockerinit.linux.service.strategy.linuxCommandStrategy.linuxCommandStrategyImpl;

import com.dockerinit.linux.domain.LinuxCommand;
import com.dockerinit.linux.model.AcPhase;
import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.model.Suggestion;
import com.dockerinit.linux.model.SuggestionType;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.service.strategy.linuxCommandStrategy.LinuxCommandStrategy;
import com.dockerinit.linux.util.RedisKeys;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
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

import static com.dockerinit.linux.model.AcPhase.*;

@Component
@RequiredArgsConstructor
public class CommonLinuxCommandStrategy implements LinuxCommandStrategy {

    private final LinuxCommandRepository repository;
    private final RedisTemplate<String, String> redis;

    @Override
    public boolean supports(String command) {
        return true; // TODO 일단 모든 명령어 처리, 이후 Common Linux Command 리스트 만들어서 contains 로 로직 변경
    }

    // TODO 추후 parse 코드 전략 패턴으로 리팩토링 계획
    @Override
    public ParseCtx parse(String line, int cursor, List<ShellTokenizer.Token> tokens) {

        if (tokens.isEmpty()) {
            return new ParseCtx(AcPhase.COMMAND, "", "", null);
        }

        String baseCommand = tokens.get(0).text();

        ShellTokenizer.Token current = tokens.get(tokens.size() - 1);
        String currentToken = current.text();
        String prevFlag = findPreviousFlag(tokens);

        AcPhase phase = decidePhase(tokens, currentToken, prevFlag, baseCommand);

        return new ParseCtx(phase, baseCommand, currentToken, prevFlag);
    }

    @Override
    public List<Suggestion> suggest(ParseCtx ctx) {
        return switch (ctx.phase()) {
            case COMMAND -> suggestCommand(ctx);
            case OPTION -> suggestOption(ctx);
            case ARGUMENT -> suggestArgument(ctx);
            case OPTION_OR_ARGUMENT -> suggestOptionOrArgument(ctx);
            default -> List.of();
        };
    }

    private List<Suggestion> suggestCommand(ParseCtx ctx) {
        String prefix = ctx.currentToken();
        String key = RedisKeys.autoCompleteCommand();

        List<String> res = fetchFromRedis(key, prefix);
        if (res.isEmpty()) {
            res = repository.findTop15ByCommandStartingWith(prefix)
                    .stream().map(c -> c.getCommand()).toList();
            cacheZSet(key, res);
        }

        redis.opsForZSet().incrementScore(RedisKeys.hotCommand(), prefix, 1);
        return res.stream().map(cmd -> new Suggestion(cmd, cmd, "", SuggestionType.COMMAND)).toList();
    }

    private List<Suggestion> suggestOption(ParseCtx ctx) {
        String prefix = ctx.currentToken();
        String key = RedisKeys.authCompleteOption(ctx.baseCommand());

        List<String> res = fetchFromRedis(key, prefix);
        if (res.isEmpty()) {
            repository.findByCommand(ctx.baseCommand()).ifPresent(cmd ->
                    cacheZSet(key, cmd.getOptions().keySet()));
            res = fetchFromRedis(key, prefix);
        }

        redis.opsForZSet().incrementScore(RedisKeys.hotOption(ctx.baseCommand()), prefix, 1);

        return res.stream()
                .map(opt -> new Suggestion(
                        opt,
                        opt + " " + placeholder(ctx.baseCommand(), opt),
                        cmdOptionDesc(ctx.baseCommand(), opt), SuggestionType.OPTION))
                .toList();
    }

    private List<Suggestion> suggestArgument(ParseCtx ctx) {
        String ph = placeholder(ctx.baseCommand(), ctx.prevFlag());
        return List.of(new Suggestion(ph, ph, "인자 placeholder", SuggestionType.ARGUMENT));
    }

    private List<Suggestion> suggestOptionOrArgument(ParseCtx ctx) {
        return Stream.concat(suggestOption(ctx).stream(), suggestArgument(ctx).stream()).toList();
    }

    private List<String> fetchFromRedis(String key, String prefix) {
        Range<String> range = Range.closed(prefix, prefix + "\uFFFF");
        Limit limit = Limit.limit().count(15);
        Set<String> strings = redis.opsForZSet()
                .rangeByLex(key, range, limit);

        return strings == null ? List.of() : new ArrayList<>(strings);
    }

    private void cacheZSet(String key, Collection<String> values) {
        if (values.isEmpty()) return;
        Set<ZSetOperations.TypedTuple<String>> tuples = values.stream()
                .map(v -> new org.springframework.data.redis.core.DefaultTypedTuple<>(v, 0.0))
                .collect(Collectors.toSet());
        redis.opsForZSet().add(key, tuples);
    }

    private String placeholder(String cmd, String flag) {
        return repository.findByCommand(cmd)
                .map(c -> c.getOptions().get(flag))
                .map(opt -> "<" + opt.argName() + ">")
                .orElse("<arg>");
    }

    private String cmdOptionDesc(String cmd, String flag) {
        return repository.findByCommand(cmd)
                .map(c -> c.getOptions().get(flag))
                .map(LinuxCommand.OptionInfo::description)
                .orElse("");
    }

    private AcPhase decidePhase(List<ShellTokenizer.Token> tokens, String currentToken, String prevFlag, String baseCommand) {

        // 명령어만 입력된 상태
        if (tokens.size() == 1) {
            return AcPhase.COMMAND;
        }

        if (currentToken.startsWith("-")) {
            // 이전 플래그가 있고, 그 플래그가 인자를 요구한다면 → currentToken은 ARGUMENT일 가능성 있음
            if (prevFlag != null && optionRequiresArg(baseCommand, prevFlag)) {
                return AcPhase.OPTION_OR_ARGUMENT; // - 다음에 오는 "-"는 ambiguous
            }
            return OPTION;
        }

        if (prevFlag != null && optionRequiresArg(baseCommand, prevFlag)) {
            return ARGUMENT;
        }

        return TARGET; // 그 외는 파일/대상 자동완성으로 간주
    }

    private String findPreviousFlag(List<ShellTokenizer.Token> tokens) {
        for (int i = tokens.size() - 2; i >= 1; i--) {
            String val = tokens.get(i).text();
            if (val.startsWith("-")) return val;
        }
        return null;
    }

    private boolean optionRequiresArg(String command, String flag) {
        return repository.findByCommand(command)
                .map(cmd -> cmd.getOptions().getOrDefault(flag, null))
                .map(LinuxCommand.OptionInfo::argRequired)
                .orElse(false);
    }


}
