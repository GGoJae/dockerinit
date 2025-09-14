package com.dockerinit.linux.application.autocomplete.suggester.types;

import com.dockerinit.linux.application.autocomplete.model.ExpectedToken;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.dto.response.common.SuggestionType;
import com.dockerinit.linux.dto.response.autocompleteV1.Suggestion;
import com.dockerinit.linux.infrastructure.repository.LinuxCommandRepository;
import com.dockerinit.linux.infrastructure.redis.RedisKeys;
import com.dockerinit.linux.application.autocomplete.replace.Replace;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.dockerinit.global.constants.Modules.LINUX;

@Component
@RequiredArgsConstructor
public class CommandTypeSuggester implements TypeSuggester {

    private final LinuxCommandRepository repository;
    private final StringRedisTemplate redis;

    private static final String MODULE = LINUX;

    @Override
    public SuggestionType type() {
        return SuggestionType.COMMAND;
    }

    @Override
    public List<Suggestion> collect(ParseResult ctx, List<ShellTokenizer.Token> tokens, ExpectedToken slot, Replace.Range range, int limit) {
        String prefix = ctx.currentToken();

        String key = RedisKeys.acCmdZSet(MODULE);

        if (prefix.isBlank()) {
            return List.of();
            // TODO Redis 에서 HOT 15 명령어 보여주기
        }

        List<String> res = fetchFromRedisOrRep(key, prefix, limit);

        return res.stream().map(cmd -> new Suggestion(cmd, cmd, "", SuggestionType.COMMAND, 0.9, range.start(), range.end())).toList();
    }

    private List<String> fetchFromRedisOrRep(String key, String prefix, int limit) {
        List<String> res = fetchFromRedis(key, prefix, limit);
        if (res.isEmpty()) {
            res = repository.findAllByCommandStartingWith(prefix, PageRequest.of(0, limit))
                    .stream().map(LinuxCommand::getCommand).toList();
        }
        return res;
    }

    private List<String> fetchFromRedis(String key, String prefix, int limitCount) {
        Range<String> range = Range.closed(prefix, prefix + "\uFFFF");
        Limit limit = Limit.limit().count(limitCount);
        Set<String> strings = redis.opsForZSet().rangeByLex(key, range, limit);

        return (strings == null) ? List.of() : new ArrayList<>(strings);
    }
}
