package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteStratyImpl.commonLinuxCommandStrategies;

import com.dockerinit.linux.application.autoComplete.model.ExpectedToken;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.dto.response.v2.SuggestionV2;
import com.dockerinit.linux.dto.vo.ParseCtx;
import com.dockerinit.linux.dto.response.Suggestion;
import com.dockerinit.linux.dto.response.SuggestionType;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteSuggester;
import com.dockerinit.linux.util.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonLinuxCommandSuggester implements AutoCompleteSuggester {

    private final RedisTemplate<String, String> redis;
    private final LinuxCommandRepository repository;



    @Override
    public List<SuggestionV2> suggest(ParseResult result) {

        List<ExpectedToken> slots = result.expected().stream().sorted().toList();
        LinkedHashMap<String, SuggestionV2> out = new LinkedHashMap<>();

        for (ExpectedToken slot : slots) {
            switch (slot.type()) {
                case COMMAND -> suggestCommandV2(result).forEach(s -> out.putIfAbsent(s.value(), s));
                case OPTION, FLAG -> suggestOptionV2(result).forEach(s -> out.putIfAbsent(s.value(), s));
                case ARGUMENT, VALUE -> suggestArgumentV2(result).forEach(s -> out.putIfAbsent(s.value(), s));
                case FILE, DIRECTORY, PATH, SOURCE, DESTINATION -> suggestTargetV2(result)
                        .forEach(s -> out.putIfAbsent(s.value(), s));
            }
            if (out.size() >= 15) break;
        }

        return new ArrayList<>(out.values());     // TODO suggest 로직 작성
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
                .map(Option::description)
                .orElseGet(() -> "");
    }

    /*
            TODO V2 버전 호환 성공하면 위 v1 지우기
     */
    private List<SuggestionV2> suggestCommandV2(ParseResult ctx) {
        String prefix = ctx.currentToken() == null ? "" : ctx.currentToken();
        String key = RedisKeys.autoCompleteCommand(prefix);
        List<String> res = fetchFromRedis(key, prefix);
        if (res.isEmpty()) {
            res = repository.findTop15ByCommandStartingWith(prefix).stream().map(c -> c.getCommand()).toList();
            cacheZSet(key, res);
        }
        redis.opsForZSet().incrementScore(RedisKeys.hotCommand(), prefix, 1);
        return res.stream()
                .map(cmd -> new SuggestionV2(cmd, cmd, "", com.dockerinit.linux.dto.response.SuggestionType.COMMAND, 0.9, null, null))
                .toList();
    }

    private List<SuggestionV2> suggestOptionV2(ParseResult ctx) {
        String prefix = ctx.currentToken() == null ? "" : ctx.currentToken();
        String key = RedisKeys.authCompleteOption(ctx.baseCommand());
        List<String> res = fetchFromRedis(key, prefix);
        if (res.isEmpty() && ctx.command() != null) {
            cacheZSet(key, ctx.command().getOptions().keySet());
            res = fetchFromRedis(key, prefix);
        }
        redis.opsForZSet().incrementScore(RedisKeys.hotOption(ctx.baseCommand()), prefix, 1);
        return res.stream().map(flag -> {
            String ph = placeholder(ctx.baseCommand(), flag);
            String desc = cmdOptionDesc(ctx.baseCommand(), flag);
            return new SuggestionV2(flag, flag + (ph.equals("<arg>") ? "" : " " + ph), desc,
                    SuggestionType.OPTION, 0.85, null, null);
        }).toList();
    }

    private List<SuggestionV2> suggestArgumentV2(ParseResult ctx) {
        String ph = placeholder(ctx.baseCommand(), ctx.prevFlag());
        return List.of(new SuggestionV2(ph, ph, "인자 placeholder",
                SuggestionType.ARGUMENT, 0.8, null, null));
    }

    private List<SuggestionV2> suggestTargetV2(ParseResult ctx) {
        String cur = ctx.currentToken() == null ? "" : ctx.currentToken();
        var list = new java.util.ArrayList<SuggestionV2>();
        for (String v : List.of("./", "../")) {
            if (cur.isEmpty() || v.startsWith(cur) || cur.startsWith(v)) {
                list.add(new SuggestionV2(v, v, "path", SuggestionType.TARGET, 0.7, null, null));
            }
            if (list.size() >= 10) break;
        }
        return list;
    }
}
