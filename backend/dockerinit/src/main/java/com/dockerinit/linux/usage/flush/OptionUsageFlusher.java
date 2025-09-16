package com.dockerinit.linux.usage.flush;


import com.dockerinit.linux.usage.domain.OptionUsage;
import com.dockerinit.linux.usage.support.OptionUsageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
@Profile({"local", "dev", "staging", "prod"})
public class OptionUsageFlusher {

    private final StringRedisTemplate redis;
    private final MongoTemplate mongo;

    // HGETALL + DEL 원자 스냅샷
    private static final DefaultRedisScript<List> SNAPSHOT = new DefaultRedisScript<>("""
        local t = redis.call('HGETALL', KEYS[1]);
        if #t > 0 then redis.call('DEL', KEYS[1]); end;
        return t;
        """, List.class);

    @Scheduled(cron = "0 */10 * * * *") // 10분마다
    public void flushOptionDeltas() {
        Set<String> cmds = redis.opsForSet().members(OptionUsageKeys.dirtySet());
        if (cmds == null || cmds.isEmpty()) return;

        for (String cmd : cmds) {
            String hkey = OptionUsageKeys.hDelta(cmd);
            Map<String, Long> deltas = snapshot(hkey);
            if (deltas.isEmpty()) {
                // 델타 없으면 dirty 제거
                redis.opsForSet().remove(OptionUsageKeys.dirtySet(), cmd);
                continue;
            }
            // Mongo bulk upsert
            BulkOperations bulk = mongo.bulkOps(BulkOperations.BulkMode.UNORDERED, OptionUsage.class);
            Instant now = Instant.now();
            deltas.forEach((flag, inc) -> {
                Query q = Query.query(Criteria.where("commandNorm").is(cmd).and("flag").is(flag));
                Update u = new Update()
                        .inc("count", inc)
                        .set("updatedAt", now);
                bulk.upsert(q, u);
            });
            bulk.execute();

            // 처리 완료 → dirty 제거
            redis.opsForSet().remove(OptionUsageKeys.dirtySet(), cmd);
        }
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
