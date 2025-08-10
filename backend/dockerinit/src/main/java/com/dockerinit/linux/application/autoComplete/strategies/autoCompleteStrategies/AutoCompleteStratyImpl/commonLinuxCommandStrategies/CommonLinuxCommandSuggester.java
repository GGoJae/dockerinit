package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteStratyImpl.commonLinuxCommandStrategies;

import com.dockerinit.linux.application.autoComplete.SuggestionMapping;
import com.dockerinit.linux.application.autoComplete.model.CommandView;
import com.dockerinit.linux.application.autoComplete.model.ExpectedToken;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteSuggester;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.dto.response.SuggestionType;
import com.dockerinit.linux.dto.response.v2.SuggestionV2;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.util.RedisKeys;
import com.dockerinit.linux.util.Replace;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.dockerinit.global.constants.AutoCompleteSuggest.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonLinuxCommandSuggester implements AutoCompleteSuggester {

    private final RedisTemplate<String, String> redis;
    private final LinuxCommandRepository repository;

    @Override
    public List<SuggestionV2> suggest(ParseResult result, List<ShellTokenizer.Token> tokens) {
        Objects.requireNonNull(result, "ParseResult");

        List<ExpectedToken> slots = result.expected().stream().sorted().toList();
        LinkedHashMap<String, SuggestionV2> out = new LinkedHashMap<>();

        if (slots.isEmpty() && result.command() == null && result.tokenIndex() <= 0) {
            suggestCommand(result, tokens).forEach(s -> out.putIfAbsent(mapKeyGenerator(s), s));
        }

        for (ExpectedToken slot : slots) {
            SuggestionType suggestionType = SuggestionMapping.fromTokenType(slot.type());
            switch (suggestionType) {
                case COMMAND -> suggestCommand(result, tokens)
                        .forEach(s -> out.putIfAbsent(mapKeyGenerator(s), s));
                case OPTION -> suggestOption(result, tokens)
                        .forEach(s -> out.putIfAbsent(mapKeyGenerator(s), s));
                case ARGUMENT-> suggestArgument(result, tokens)
                        .forEach(s -> out.putIfAbsent(mapKeyGenerator(s), s));
                case TARGET -> suggestTarget(result, tokens)
                        .forEach(s -> out.putIfAbsent(mapKeyGenerator(s), s));
            }
            if (out.size() >= MAX_SUGGEST) break;
        }

        return new ArrayList<>(out.values());
    }

    private List<String> fetchFromRedis(String key, String prefix) {
        Range<String> range = Range.closed(prefix, prefix + "\uFFFF");
        Limit limit = Limit.limit().count(MAX_SUGGEST);
        Set<String> strings = redis.opsForZSet()
                .rangeByLex(key, range, limit);

        return strings == null ? List.of() : new ArrayList<>(strings);
    }

    private void cacheZSet(String key, Collection<String> values) {
        if (values.isEmpty()) return;
        Set<ZSetOperations.TypedTuple<String>> tuples = values.stream()
                .map(v -> new DefaultTypedTuple<>(v, 0.0))
                .collect(Collectors.toSet());
        redis.opsForZSet().add(key, tuples);
    }

    private String placeholder(CommandView cmd, String flag) {
        Option option = (cmd != null) ? cmd.options().get(flag) : null;
        return (option == null) ? PLACE_HOLDER : "<" + option.argName() + ">";
    }

    private String cmdOptionDesc(CommandView cmd, String flag) {
        Option option = cmd == null ? null : cmd.options().get(flag);
        return  option == null ? "" : option.description();
    }

    private List<SuggestionV2> suggestCommand(ParseResult ctx, List<ShellTokenizer.Token> tokens) {
        String prefix = ctx.currentToken();
        Replace.Range range = Replace.forCurrentToken(ctx.cursor(), tokens);

        if (prefix.isBlank()) {
            return List.of();
            // TODO prefix 가 비어있을떄(커맨드 치기전) hot 15  정도 zset 에서 노출하기
        }

        String key = RedisKeys.autoCompleteCommand(prefix);
        List<String> res = fetchFromRedis(key, prefix);
        if (res.isEmpty()) {
            res = repository
                    .findTop15ByCommandStartingWith(prefix)
                    .stream()
                    .map(c -> c.getCommand()).toList();
            cacheZSet(key, res);
        }

        return res.stream()
                .map(cmd ->
                        new SuggestionV2(cmd, cmd, "", SuggestionType.COMMAND,
                                0.9, range.start(), range.end()))
                .toList();
    }

    private List<SuggestionV2> suggestOption(ParseResult ctx, List<ShellTokenizer.Token> tokens) {
        String prefix = ctx.currentToken();
        String key = RedisKeys.autoCompleteOption(ctx.baseCommand());

        Map<String, Option> optionMap = ctx.command() == null ? Map.of() : ctx.command().options();
        if (optionMap.isEmpty()) return List.of();

        List<String> fromRedis = List.of();
        try {
            if (!prefix.isBlank()) {
                fromRedis = fetchFromRedis(key, prefix);
            }
        } catch (Exception e) {
            log.debug("redis miss/error : {}", e.getMessage());
        }

        Set<String> flags = new LinkedHashSet<>();
        if (!fromRedis.isEmpty()) {
            flags.addAll(fromRedis);
        }
        optionMap.keySet().stream()
                .filter(f -> prefix.isBlank() || f.startsWith(prefix))
                .forEach(flags::add);


        Replace.Range range = Replace.forCurrentToken(ctx.cursor(), tokens);

        return flags.stream()
                .limit(MAX_SUGGEST)
                .map(flag -> {
                    Option o = optionMap.get(flag);
                    String ph = (o != null && o.argName() != null) ? "<" + o.argName() + ">" : PLACE_HOLDER;
                    String disp = flag + (PLACE_HOLDER.equals(ph) ? "" : " " + ph);
                    String desc = (o == null || o.description() == null) ? "" : o.description();
                    return new SuggestionV2(flag, disp, desc, SuggestionType.OPTION, 0.85, range.start(), range.end());
                }).toList();
//        CommandView cmd = ctx.command();
//        return res.stream().map(flag -> {
//            String ph = placeholder(cmd, flag);
//            String desc = cmdOptionDesc(cmd, flag);
//            return new SuggestionV2(flag, flag + (ph.equals(PLACE_HOLDER) ? "" : " " + ph), desc,
//                    SuggestionType.OPTION, 0.85, range.start(), range.end());
//        }).toList();
    }

    private List<SuggestionV2> suggestArgument(ParseResult ctx, List<ShellTokenizer.Token> tokens ) {
        String ph = placeholder(ctx.command(), ctx.prevFlag());
        Replace.Range range = Replace.forCurrentToken(ctx.cursor(), tokens);
        return List.of(new SuggestionV2(ph, ph, "인자 placeholder",
                SuggestionType.ARGUMENT, 0.8, range.start(), range.end()));
    }

    private List<SuggestionV2> suggestTarget(ParseResult ctx, List<ShellTokenizer.Token> tokens) {
        String cur = ctx.currentToken();
        List<SuggestionV2> out = new ArrayList<SuggestionV2>();
        Replace.Range range = Replace.forCurrentToken(ctx.cursor(), tokens);
        for (String v : List.of("./", "../")) {
            if (cur.isEmpty() || v.startsWith(cur) || cur.startsWith(v)) {
                out.add(new SuggestionV2(v, v, PATH_DESC, SuggestionType.TARGET, 0.7, range.start(), range.end()));
            }
            if (out.size() >= 10) break;
        }
        return out;
    }

    private static String mapKeyGenerator(SuggestionV2 suggestion) {
        return suggestion.type().name() + "\u0000" + suggestion.value();
    }
}
