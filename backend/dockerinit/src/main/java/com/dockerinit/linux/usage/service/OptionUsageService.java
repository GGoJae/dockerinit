package com.dockerinit.linux.usage.service;

import com.dockerinit.linux.usage.domain.OptionUsage;
import com.dockerinit.linux.usage.repository.OptionUsageRepository;
import com.dockerinit.linux.usage.support.OptionUsageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionUsageService {

    private final StringRedisTemplate redis;
    private final OptionUsageRepository repository;

    public void trackOptionPick(String commandNorm, String flag) {
        String cmd = normalize(commandNorm);
        String f = canonicalFlag(flag);

        redis.opsForZSet().incrementScore(OptionUsageKeys.zRank(cmd), f, 1.0);
        redis.opsForHash().increment(OptionUsageKeys.hDelta(cmd), f, 1L);
        redis.opsForSet().add(OptionUsageKeys.dirtySet(), cmd);

        redis.expire(OptionUsageKeys.zRank(cmd), Duration.ofDays(30));
    }

    public List<String> topFlags(String commandNorm, int topN) {
        String cmd = normalize(commandNorm);
        String zKey = OptionUsageKeys.zRank(cmd);

        Set<String> fromRedis = Objects.requireNonNull(redis.opsForZSet().reverseRange(zKey, 0, Math.max(0, topN - 1)))
                .stream().map(String::valueOf).collect(Collectors.toCollection(LinkedHashSet::new));
        if (fromRedis.isEmpty()) return List.of();

        List<OptionUsage> usage = repository.findByCommandNormOrderByCountDesc(cmd, PageRequest.of(0, topN));
        if (usage.isEmpty()) return List.of();

        usage.forEach(u -> redis.opsForZSet().add(zKey, u.getFlag(), u.getCount()));
        redis.expire(zKey, Duration.ofHours(12));

        return usage.stream().map(OptionUsage::getFlag).toList();
    }

    private static String normalize(String s) { return s == null ? "" : s.trim().toLowerCase(Locale.ROOT); }

    /** 플래그 정규화 정책(필요 시 alias→대표 치환 로직 추가) */
    private static String canonicalFlag(String f) { return f == null ? "" : f.trim(); }
}
