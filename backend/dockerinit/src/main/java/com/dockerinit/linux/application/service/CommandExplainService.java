package com.dockerinit.linux.application.service;

import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.application.explain.strategy.explainStrategy.ExplainStrategy;
import com.dockerinit.linux.application.shared.model.ModuleType;
import com.dockerinit.linux.application.shared.model.ModuleTypeMapper;
import com.dockerinit.linux.dto.response.ExplainResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommandExplainService {

    private final RedisTemplate<String, String> redis;
    private final Map<ModuleType, ExplainStrategy> strategyMap;

    public CommandExplainService(RedisTemplate<String, String> redis,List<ExplainStrategy> strategies) {
        this.redis = redis;
        this.strategyMap = strategies.stream().collect(Collectors.toMap(
                ExplainStrategy::type,
                s -> s,
                (a, b) -> a,
                () -> new EnumMap<>(ModuleType.class)
        ));
    }


    public ExplainResponse explain(@NotBlank String line, Locale loc) {

        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);
        String base = tokens.isEmpty() ? "" : tokens.get(0).text().toLowerCase(Locale.ROOT);

        ModuleType moduleType = ModuleTypeMapper.fromCommand(base);

        ExplainStrategy strategy = strategyMap.get(moduleType);
        ParseResult parsed = strategy.parse(line, line.length(), tokens);

        ExplainResponse explain = strategy.explain(parsed, loc);
        // TODO 레디스에서 조회하고 없으면 저장하는 로직 작성
        return  explain;
    }
}
