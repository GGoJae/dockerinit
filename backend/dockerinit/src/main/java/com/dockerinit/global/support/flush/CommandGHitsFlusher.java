package com.dockerinit.global.support.flush;

import com.dockerinit.linux.application.service.CommandExplainService;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.mongodb.bulk.BulkWriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandGHitsFlusher {

    private final StringRedisTemplate redis;
    private final MongoTemplate mongo;

    private static final String LOCK_KEY = "di:locks:flush_hits";
    private static final Duration LOCK_TTL = Duration.ofSeconds(55);

    private static final DefaultRedisScript<List> SNAPSHOT_SCRIPT = new DefaultRedisScript<>(
            "local t = redis.call('HGETALL', KEYS[1]); " +
                    "if #t > 0 then redis.call('DEL', KEYS[1]); end; " +
                    "return t;", List.class
    );

    @SuppressWarnings("unchecked")
    @Scheduled(fixedDelay = 30_000) // 30초마다
    public void flush() {
        if (!acquireLock()) return;
        try {
            List<Object> raw = redis.execute(SNAPSHOT_SCRIPT, List.of(CommandExplainService.HITS_HASH_KEY));
            if (raw.isEmpty()) return;

            // HGETALL 응답 → Map<String, Long>
            Map<String, Long> deltas = toMap(raw);

            try {
                bulkIncreaseSearchCount(deltas);
            } catch (Exception dbEx) {
                log.error("DB flush failed, restoring deltas to Redis …", dbEx);
                // 롤백: Redis에 다시 누적
                restoreToRedis(deltas);
            }
        } finally {
            releaseLock();
        }
    }

    private boolean acquireLock() {
        try {
            Boolean ok = redis.opsForValue().setIfAbsent(LOCK_KEY, UUID.randomUUID().toString(), LOCK_TTL);
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            return false;
        }
    }
    private void releaseLock() { redis.delete(LOCK_KEY); }

    @SuppressWarnings("unchecked")
    private Map<String, Long> toMap(List<Object> raw) {
        Map<String, Long> m = new LinkedHashMap<>();
        // raw = [field1, val1, field2, val2, ...]
        for (int i = 0; i < raw.size(); i += 2) {
            String field = String.valueOf(raw.get(i));
            long val = Long.parseLong(String.valueOf(raw.get(i + 1)));
            if (val != 0) m.put(field, val);
        }
        return m;
    }

    private void restoreToRedis(Map<String, Long> deltas) {
        deltas.forEach((base, inc) -> redis.opsForHash().increment(
                CommandExplainService.HITS_HASH_KEY, base, inc));
    }

    private void bulkIncreaseSearchCount(Map<String, Long> deltas) {
        if (deltas.isEmpty()) return;

        BulkOperations bulk = mongo.bulkOps(BulkOperations.BulkMode.UNORDERED, LinuxCommand.class);
        deltas.forEach((base, inc) -> {
            Query q = new Query(Criteria.where("command").is(base));
            Update u = new Update().inc("searchCount", inc);
            bulk.updateOne(q, u);
        });
        BulkWriteResult res = bulk.execute();
        log.info("Flushed hits: {}", res);
    }
}
