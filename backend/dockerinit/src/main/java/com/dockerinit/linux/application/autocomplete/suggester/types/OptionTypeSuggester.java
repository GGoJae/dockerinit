package com.dockerinit.linux.application.autocomplete.suggester.types;

import com.dockerinit.linux.application.autocomplete.model.ExpectedToken;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.dto.response.common.SuggestionType;
import com.dockerinit.linux.dto.response.autocompleteV1.Suggestion;
import com.dockerinit.linux.infrastructure.redis.RedisKeys;
import com.dockerinit.linux.application.autocomplete.replace.Replace;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.dockerinit.global.constants.AutoCompleteSuggest.PLACE_HOLDER;
import static com.dockerinit.global.constants.Modules.LINUX;

@Component
@RequiredArgsConstructor
public class OptionTypeSuggester implements TypeSuggester {

    private final RedisTemplate<String, String> redis;

    private static final String MODULE = LINUX;

    @Override
    public SuggestionType type() {
        return SuggestionType.OPTION;
    }

    @Override
    public List<Suggestion> collect(ParseResult ctx, List<ShellTokenizer.Token> tokens, ExpectedToken slot, Replace.Range range, int limit) {
        String prefix = ctx.currentToken();
        String key = RedisKeys.acOptZSet(MODULE, ctx.baseCommand());

        Map<String, Option> optionMap = ctx.optionOrEmpty();
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
            return new Suggestion(flag, disp, desc, SuggestionType.OPTION, 0.85, range.start(), range.end());
        }).toList();
    }

    private List<String> fetchFromRedis(String key, String prefix, int limitCount) {
        Range<String> range = Range.closed(prefix, prefix + "\uFFFF");
        Limit limit = Limit.limit().count(limitCount);
        Set<String> strings = redis.opsForZSet().rangeByLex(key, range, limit);

        return strings == null ? List.of() : new ArrayList<>(strings);
    }
}
