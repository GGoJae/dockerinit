package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteStrategyImpl.commonLinuxCommandStrategyImpl;

import com.dockerinit.linux.application.autoComplete.model.SuggestionMapping;
import com.dockerinit.linux.application.autoComplete.model.ExpectedToken;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteSuggester;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.TypeSuggester;
import com.dockerinit.linux.domain.syntax.TokenType;
import com.dockerinit.linux.dto.response.SuggestionType;
import com.dockerinit.linux.dto.response.v2.Suggestion;
import com.dockerinit.linux.util.Replace;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.dockerinit.global.constants.AutoCompleteSuggest.MAX_SUGGEST;

@Slf4j
@Component
public class CommonLinuxCommandSuggester implements AutoCompleteSuggester {

    private final Map<SuggestionType, TypeSuggester> registry;

    public CommonLinuxCommandSuggester(List<TypeSuggester> delegates) {
        this.registry = delegates.stream().collect(Collectors.toMap(
                d -> d.type(),
                d -> d,
                (a, b) -> a,
                () -> new EnumMap<>(SuggestionType.class)
        ));
    }

    @Override
    public List<Suggestion> suggest(ParseResult result, List<ShellTokenizer.Token> tokens) {
        Objects.requireNonNull(result, "Parse Result is Non null");

        List<ExpectedToken> slots = result.expected().stream().sorted().toList();
        LinkedHashMap<DedupKey, Suggestion> out = new LinkedHashMap<>();

        if (slots.isEmpty() && result.command() == null && result.tokenIndex() <= 0) {
            log.debug("슬롯이 비어있어서 COMMAND 를 주입했습니다. slot for prefix='{}'", result.currentToken());
            slots = List.of(new ExpectedToken(TokenType.COMMAND, 0, 1.0, Map.of()));
        }

        Replace.Range range = Replace.forCurrentToken(result.cursor(), tokens);

        outer:
        for (ExpectedToken slot : slots) {
            SuggestionType st = SuggestionMapping.fromTokenType(slot.type());
            TypeSuggester typeSuggester = registry.get(st);

            if (typeSuggester == null) continue;

            List<Suggestion> collect = typeSuggester.collect(result, tokens, slot, range, MAX_SUGGEST - out.size());
            for (Suggestion s : collect) {
                out.putIfAbsent(new DedupKey(s.type(), s.value()), s);
                if (out.size() >= MAX_SUGGEST) {
                    break outer;
                }
            }
        }
        return new ArrayList<>(out.values());
//        for (ExpectedToken slot : slots) {
//            SuggestionType suggestionType = SuggestionMapping.fromTokenType(slot.type());
//            switch (suggestionType) {
//                case COMMAND -> suggestCommand(result, tokens).forEach(s -> out.putIfAbsent(mapKeyGenerator(s), s));
//                case OPTION -> suggestOption(result, tokens).forEach(s -> out.putIfAbsent(mapKeyGenerator(s), s));
//                case ARGUMENT -> suggestArgument(result, tokens).forEach(s -> out.putIfAbsent(mapKeyGenerator(s), s));
//                case TARGET -> suggestTarget(result, tokens).forEach(s -> out.putIfAbsent(mapKeyGenerator(s), s));
//            }
//            if (out.size() >= MAX_SUGGEST) break;
//        }
    }

//    private List<String> fetchFromRedis(String key, String prefix) {
//        Range<String> range = Range.closed(prefix, prefix + "\uFFFF");
//        Limit limit = Limit.limit().count(MAX_SUGGEST);
//        Set<String> strings = redis.opsForZSet().rangeByLex(key, range, limit);
//
//        return strings == null ? List.of() : new ArrayList<>(strings);
//    }
//
//    private void cacheZSet(String key, Collection<String> values) {
//        if (values.isEmpty()) return;
//        Set<ZSetOperations.TypedTuple<String>> tuples = values.stream().map(v -> new DefaultTypedTuple<>(v, 0.0)).collect(Collectors.toSet());
//        redis.opsForZSet().add(key, tuples);
//    }
//
//    private String placeholder(CommandView cmd, String flag) {
//        Option option = (cmd != null) ? cmd.options().get(flag) : null;
//        return (option == null) ? PLACE_HOLDER : "<" + option.argName() + ">";
//    }
//
//    private String cmdOptionDesc(CommandView cmd, String flag) {
//        Option option = cmd == null ? null : cmd.options().get(flag);
//        return option == null ? "" : option.description();
//    }
//
//    private List<Suggestion> suggestCommand(ParseResult ctx, List<ShellTokenizer.Token> tokens) {
//        String prefix = ctx.currentToken();
//        Replace.Range range = Replace.forCurrentToken(ctx.cursor(), tokens);
//
//        if (prefix.isBlank()) {
//            return List.of();
//            // TODO prefix 가 비어있을떄(커맨드 치기전) hot 15  정도 zset 에서 노출하기
//        }
//
//        String key = RedisKeys.autoCompleteCommand(prefix);
//        List<String> res = fetchFromRedis(key, prefix);
//        if (res.isEmpty()) {
//            res = repository.findTop15ByCommandStartingWith(prefix).stream().map(c -> c.getCommand()).toList();
//            cacheZSet(key, res);
//        }
//
//        return res.stream().map(cmd -> new Suggestion(cmd, cmd, "", SuggestionType.COMMAND, 0.9, range.start(), range.end())).toList();
//    }
//
//    private List<Suggestion> suggestOption(ParseResult ctx, List<ShellTokenizer.Token> tokens) {
//        String prefix = ctx.currentToken();
//        String key = RedisKeys.autoCompleteOption(ctx.baseCommand());
//
//        Map<String, Option> optionMap = ctx.command() == null ? Map.of() : ctx.command().options();
//        if (optionMap.isEmpty()) return List.of();
//
//        List<String> fromRedis = List.of();
//        try {
//            if (!prefix.isBlank()) {
//                fromRedis = fetchFromRedis(key, prefix);
//            }
//        } catch (Exception e) {
//            log.debug("redis miss/error : {}", e.getMessage());
//        }
//
//        Set<String> flags = new LinkedHashSet<>();
//        if (!fromRedis.isEmpty()) {
//            flags.addAll(fromRedis);
//        }
//        optionMap.keySet().stream().filter(f -> prefix.isBlank() || f.startsWith(prefix)).forEach(flags::add);
//
//
//        Replace.Range range = Replace.forCurrentToken(ctx.cursor(), tokens);
//
//        return flags.stream().limit(MAX_SUGGEST).map(flag -> {
//            Option o = optionMap.get(flag);
//            String ph = (o != null && o.argName() != null) ? "<" + o.argName() + ">" : PLACE_HOLDER;
//            String disp = flag + (PLACE_HOLDER.equals(ph) ? "" : " " + ph);
//            String desc = (o == null || o.description() == null) ? "" : o.description();
//            return new Suggestion(flag, disp, desc, SuggestionType.OPTION, 0.85, range.start(), range.end());
//        }).toList();
//    }
//
//    private List<Suggestion> suggestArgument(ParseResult ctx, List<ShellTokenizer.Token> tokens) {
//        String ph = placeholder(ctx.command(), ctx.prevFlag());
//        Replace.Range range = Replace.forCurrentToken(ctx.cursor(), tokens);
//        return List.of(new Suggestion(ph, ph, "인자 placeholder", SuggestionType.ARGUMENT, 0.8, range.start(), range.end()));
//    }
//
//    private List<Suggestion> suggestTarget(ParseResult ctx, List<ShellTokenizer.Token> tokens) {
//        String cur = ctx.currentToken();
//        List<Suggestion> out = new ArrayList<Suggestion>();
//        Replace.Range range = Replace.forCurrentToken(ctx.cursor(), tokens);
//        for (String v : List.of("./", "../")) {
//            if (cur.isEmpty() || v.startsWith(cur) || cur.startsWith(v)) {
//                out.add(new Suggestion(v, v, PATH_DESC, SuggestionType.TARGET, 0.7, range.start(), range.end()));
//            }
//            if (out.size() >= 10) break;
//        }
//        return out;
//    }

    private record DedupKey(SuggestionType type, String value) {}
}
