package com.dockerinit.linux.usage.flush;

import com.dockerinit.linux.usage.domain.CommandUsage;
import com.dockerinit.linux.usage.support.CommandUsageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Profile({"local", "dev", "staging", "prod"})
public class CommandUsageFlusher {

    private final StringRedisTemplate redis;
    private final MongoTemplate mongo;

    // HGETALL + DEL
    private static final DefaultRedisScript<List> SNAPSHOT = new DefaultRedisScript<>("""
        local t = redis.call('HGETALL', KEYS[1]);
        if #t > 0 then redis.call('DEL', KEYS[1]); end;
        return t;
        """, List.class);

    @Scheduled(cron = "0 */10 * * * *") // 10분마다
    public void flushCommandDeltas() {
        if (redis.opsForValue().get(CommandUsageKeys.dirtyKey()) == null) return;

        Map<String, Long> deltas = snapshot(CommandUsageKeys.hDelta());
        if (deltas.isEmpty()) {
            redis.delete(CommandUsageKeys.dirtyKey());
            return;
        }
        var bulk = mongo.bulkOps(BulkOperations.BulkMode.UNORDERED, CommandUsage.class);
        Instant now = Instant.now();
        deltas.forEach((cmd, inc) -> {
            Query q = Query.query(Criteria.where("commandNorm").is(cmd));
            Update u = new Update().inc("count", inc).set("updatedAt", now);
            bulk.upsert(q, u);
        });
        bulk.execute();
        redis.delete(CommandUsageKeys.dirtyKey());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> snapshot(String hkey) {
        List<String> flat = (List<String>) redis.execute(SNAPSHOT, Collections.singletonList(hkey));
        Map<String, Long> m = new LinkedHashMap<>();
        if (flat == null) return m;
        for (int i = 0; i + 1 < flat.size(); i += 2) {
            m.put(flat.get(i), Long.parseLong(flat.get(i + 1)));
        }
        return m;
    }
}
