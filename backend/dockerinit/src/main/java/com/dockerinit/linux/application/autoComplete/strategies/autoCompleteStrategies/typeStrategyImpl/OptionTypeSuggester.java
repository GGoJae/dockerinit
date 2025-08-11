package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.typeStrategyImpl;

import com.dockerinit.linux.application.autoComplete.model.ExpectedToken;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.TypeSuggester;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.dto.response.SuggestionType;
import com.dockerinit.linux.dto.response.v2.SuggestionV2;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.util.RedisKeys;
import com.dockerinit.linux.util.Replace;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.dockerinit.global.constants.AutoCompleteSuggest.PLACE_HOLDER;

@Component
@RequiredArgsConstructor
public class OptionTypeSuggester implements TypeSuggester {

    private final LinuxCommandRepository repository;
    private final RedisTemplate<String, String> redis;

    @Override
    public SuggestionType type() {
        return SuggestionType.OPTION;
    }

    @Override
    public List<SuggestionV2> collect(ParseResult ctx, List<ShellTokenizer.Token> tokens, ExpectedToken slot, Replace.Range range, int limit) {
        String prefix = ctx.currentToken();
        String key = RedisKeys.autoCompleteOption(ctx.baseCommand());

        Map<String, Option> optionMap = ctx.command() == null ? Map.of() : ctx.command().options();
//        if (optionMap.isEmpty()) return List.of();

        List<String> fromRedis = fetchFromRedis(key, prefix, limit);

        Set<String> flags = new LinkedHashSet<>();
        if (!fromRedis.isEmpty()) {
            flags.addAll(fromRedis);
        }
        optionMap.keySet().stream().filter(f -> prefix.isBlank() || f.startsWith(prefix)).forEach(flags::add);

        return flags.stream().limit(limit).map(flag -> {
            Option o = optionMap.get(flag);
            String ph = (o != null && o.argName() != null) ? "<" + o.argName() + ">" : PLACE_HOLDER;
            String disp = flag + (PLACE_HOLDER.equals(ph) ? "" : " " + ph);
            String desc = (o == null || o.description() == null) ? "" : o.description();
            return new SuggestionV2(flag, disp, desc, SuggestionType.OPTION, 0.85, range.start(), range.end());
        }).toList();
    }

    private List<String> fetchFromRedis(String key, String prefix, int limitCount) {
        Range<String> range = Range.closed(prefix, prefix + "\uFFFF");
        Limit limit = Limit.limit().count(limitCount);
        Set<String> strings = redis.opsForZSet().rangeByLex(key, range, limit);

        return strings == null ? List.of() : new ArrayList<>(strings);
    }
}
