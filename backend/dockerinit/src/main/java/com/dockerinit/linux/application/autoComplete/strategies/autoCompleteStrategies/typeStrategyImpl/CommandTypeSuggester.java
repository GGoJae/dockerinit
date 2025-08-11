package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.typeStrategyImpl;

import com.dockerinit.linux.application.autoComplete.model.ExpectedToken;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.TypeSuggester;
import com.dockerinit.linux.dto.response.SuggestionType;
import com.dockerinit.linux.dto.response.v2.SuggestionV2;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.util.RedisKeys;
import com.dockerinit.linux.util.Replace;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.dockerinit.global.constants.AutoCompleteSuggest.MAX_SUGGEST;

@Component
@RequiredArgsConstructor
public class CommandTypeSuggester implements TypeSuggester {

    private final LinuxCommandRepository repository;
    private final RedisTemplate<String, String> redis;

    @Override
    public SuggestionType type() {
        return SuggestionType.COMMAND;
    }

    @Override
    public List<SuggestionV2> collect(ParseResult ctx, List<ShellTokenizer.Token> tokens, ExpectedToken slot, Replace.Range range, int limit) {
        String prefix = ctx.currentToken();

        String key = RedisKeys.autoCompleteCommand(prefix);

        if (prefix.isBlank()) {
            return List.of();
            // TODO Redis 에서 HOT 15 명령어 보여주기
        }

        List<String> res = fetchFromRedisOrRep(key, prefix, limit);

        return res.stream().map(cmd -> new SuggestionV2(cmd, cmd, "", SuggestionType.COMMAND, 0.9, range.start(), range.end())).toList();
    }

    private List<String> fetchFromRedisOrRep(String key, String prefix, int limit) {
        List<String> res = fetchFromRedis(key, prefix, limit);
        if (res.isEmpty()) {
            res = repository.findAllByCommandStartingWith(prefix, PageRequest.of(0, limit))
                    .stream().map(c -> c.getCommand()).toList();
        }
        return res;
    }

    private List<String> fetchFromRedis(String key, String prefix, int limitCount) {
        Range<String> range = Range.closed(prefix, prefix + "\uFFFF");
        Limit limit = Limit.limit().count(limitCount);
        Set<String> strings = redis.opsForZSet().rangeByLex(key, range, limit);

        return strings == null ? List.of() : new ArrayList<>(strings);
    }
}
