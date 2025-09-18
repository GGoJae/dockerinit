package com.dockerinit.linux.application.service;

import com.dockerinit.linux.usage.domain.OptionUsage;
import com.dockerinit.linux.usage.service.OptionUsageService;
import com.dockerinit.linux.usage.support.OptionUsageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OptionRankingService {

    private final StringRedisTemplate redis;
    private final OptionUsageService ouService;

    public List<ScoredFlag> rank(String commandNorm, Collection<String> candidates, int limit) {
        String zkey = OptionUsageKeys.zRank(commandNorm.toLowerCase(Locale.ROOT));

        List<ScoredFlag> scored = new ArrayList<>(candidates.size());
        for (String f : candidates) {
            Double score = redis.opsForZSet().score(zkey, f);
            scored.add(new ScoredFlag(f, score == null ? 0.0 : score));
        }

        boolean allZero = scored.stream().allMatch(s -> s.score == 0.0);
        if (allZero) ouService.topFlags(commandNorm, 20); // Redis ZSET 시드

        scored.sort(Comparator.<ScoredFlag>comparingDouble(s -> s.score).reversed()
                .thenComparing(s -> s.flag));
        return scored.size() > limit ? scored.subList(0, limit) : scored;
    }

    public record ScoredFlag(String flag, double score) {}
}
