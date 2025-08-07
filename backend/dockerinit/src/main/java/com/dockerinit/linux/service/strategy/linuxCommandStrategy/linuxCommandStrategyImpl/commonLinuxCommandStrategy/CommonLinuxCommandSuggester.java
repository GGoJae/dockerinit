package com.dockerinit.linux.service.strategy.linuxCommandStrategy.linuxCommandStrategyImpl.commonLinuxCommandStrategy;

import com.dockerinit.linux.domain.LinuxCommand;
import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.model.Suggestion;
import com.dockerinit.linux.model.SuggestionType;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.service.strategy.linuxCommandStrategy.LinuxCommandSuggester;
import com.dockerinit.linux.util.RedisKeys;
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
public class CommonLinuxCommandSuggester implements LinuxCommandSuggester {

    private final RedisTemplate<String, String> redis;
    private final LinuxCommandRepository repository;

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
        String key = RedisKeys.autoCompleteCommand(prefix);

        log.info("prefix : {} , key : {}", prefix, key);
        List<String> res = fetchFromRedis(key, prefix);
        if (res.isEmpty()) {
            res = repository.findTop15ByCommandStartingWith(prefix)
                    .stream().map(c -> c.getCommand()).toList();
            cacheZSet(key, res);
        }
        log.info("res : {}", res);

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
                .orElseGet(() -> "<arg>");
    }

    private String cmdOptionDesc(String cmd, String flag) {
        return repository.findByCommand(cmd)
                .map(c -> c.getOptions().get(flag))
                .map(LinuxCommand.OptionInfo::description)
                .orElseGet(() -> "");
    }
}
