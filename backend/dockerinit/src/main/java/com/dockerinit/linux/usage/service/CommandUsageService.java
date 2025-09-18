package com.dockerinit.linux.usage.service;

import com.dockerinit.linux.usage.domain.CommandUsage;
import com.dockerinit.linux.usage.repository.CommandUsageRepository;
import com.dockerinit.linux.usage.support.CommandUsageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandUsageService {

    private final StringRedisTemplate redis;
    private final CommandUsageRepository repository;

    public void trackCommandPick(String commandNorm) {
        String cmd = normalize(commandNorm);
        redis.opsForZSet().incrementScore(CommandUsageKeys.zRank(), cmd, 1.0);
        redis.opsForHash().increment(CommandUsageKeys.hDelta(), cmd, 1L);
        redis.opsForValue().set(CommandUsageKeys.dirtyKey(), "1", Duration.ofMinutes(30));
    }

    public List<String> topCommands(int topN) {
        var zkey = CommandUsageKeys.zRank();
        var fromRedis = redis.opsForZSet().reverseRange(zkey, 0, Math.max(0, topN - 1));
        if (fromRedis != null && !fromRedis.isEmpty()) {
            return fromRedis.stream().map(String::valueOf).collect(Collectors.toList());
        }
        var usage = repository.findAllByOrderByCountDesc(PageRequest.of(0, topN));
        if (usage.isEmpty()) return List.of();
        usage.forEach(u -> redis.opsForZSet().add(zkey, u.getCommandNorm(), (double) u.getCount()));
        redis.expire(zkey, Duration.ofHours(12));
        return usage.stream().map(CommandUsage::getCommandNorm).toList();
    }

    private static String normalize(String s) { return s == null ? "" : s.trim().toLowerCase(Locale.ROOT); }
}
