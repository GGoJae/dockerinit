package com.dockerinit.linux.application.service;

import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.explain.strategy.explainStrategy.ExplainStrategy;
import com.dockerinit.linux.application.shared.model.ModuleType;
import com.dockerinit.linux.application.shared.model.ModuleTypeMapper;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.dto.response.explainV1.ExplainResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommandExplainService {

    private static final String EXPLAIN_KEY_FMT = "di:linux:explain:%s:%s"; // base:locale
    private static final String MISS_KEY_FMT    = "di:linux:explain:miss:%s";
    private static final Duration EXPLAIN_TTL   = Duration.ofHours(6);
    private static final Duration MISS_TTL      = Duration.ofMinutes(5);
    public static final String HITS_HASH_KEY = "di:linux:cmd:hits";

    private final Map<ModuleType, ExplainStrategy> strategyMap;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public CommandExplainService(List<ExplainStrategy> strategies, StringRedisTemplate redis, ObjectMapper mapper) {
        this.strategyMap = strategies.stream().collect(Collectors.toMap(
                ExplainStrategy::type,
                s -> s,
                (a, b) -> a,
                () -> new EnumMap<>(ModuleType.class)
        ));
        this.redis = redis;
        this.mapper = mapper;
    }


    public ExplainResponse explain(@NotBlank String line, Locale loc) {

        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);
        String base = tokens.isEmpty() ? "" : tokens.get(0).text().toLowerCase(Locale.ROOT);

        String cacheKey = EXPLAIN_KEY(base, loc);
        String missKey = MISS_KEY(base);

        if (Boolean.TRUE.equals(redis.hasKey(missKey))) {
            // TODO 레디스 미스키에 값이 있으면 어떻게 할것인가...  작성
        }

        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return mapper.readValue(cached, ExplainResponse.class);
            } catch (Exception e) {
                // TODO 캐시가 깨졌다..?? 무시 할 것 인가?
            }
        }

        ModuleType moduleType = ModuleTypeMapper.fromCommand(base);
        ExplainStrategy strategy = strategyMap.get(moduleType);
        ParseResult parsed = strategy.parse(line, line.length(), tokens);
        ExplainResponse explain = strategy.explain(parsed, loc);

        try {
            redis.opsForValue().set(cacheKey, mapper.writeValueAsString(explain), EXPLAIN_TTL);
        } catch (Exception e) {
            // TODO 캐시 저장에 실패했는데 무시할까?
        }

        if (explain.isEmpty()) {
            redis.opsForValue().set(missKey, "1", MISS_TTL);
        }

        redis.opsForHash().increment(HITS_HASH_KEY, base, 1);

        return  explain;
    }

    private static String EXPLAIN_KEY(String base, Locale loc) {
        return EXPLAIN_KEY_FMT.formatted(base, loc.toLanguageTag());
    }
    private static String MISS_KEY(String base) { return MISS_KEY_FMT.formatted(base); }

}
