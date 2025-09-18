package com.dockerinit.linux.usage.materialize;

import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.usage.domain.CommandUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile({"local", "dev", "staging", "prod"})
@Slf4j
public class CommandSearchCountMaterializer {

    private static final String STATE_ID = "command_usage_materialize";
    private static final int BATCH = 1000;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MongoTemplate mongo;
    private final JobStateRepository stateRepo;

    /** 매일 03:10 KST 에 증분 머티리얼라이즈 (updatedAt 기반) */
    @Scheduled(cron = "0 10 3 * * *", zone = "Asia/Seoul")
    public void materializeIncremental() {
        Instant started = Instant.now();
        JobState st = stateRepo.findById(STATE_ID).orElse(new JobState(STATE_ID, null));
        Instant since = st.getLastSyncedAt();
        // 안전버퍼 60초(지연 기록 대비)
        Instant windowStart = since == null ? Instant.EPOCH : since.minusSeconds(60);

        long updated = syncFromUsage(windowStart, null); // until=null → 이제 시점까지
        st.setLastSyncedAt(started);
        stateRepo.save(st);

        log.info("[materialize] incremental updated={} since={} now={}",
                updated, windowStart.atZone(KST), started.atZone(KST));
    }

    /** 필요 시 전체 리셋 머티리얼라이즈 (수동 호출/임시 운영용) */
    public long materializeFull() {
        return syncFromUsage(Instant.EPOCH, null);
    }

    /**
     * CommandUsage.updatedAt ∈ [since, until) 범위만 읽어 LinuxCommand.searchCount = usage.count 로 세팅
     * @return 반영된 도큐먼트 개수
     */
    private long syncFromUsage(Instant since, Instant until) {
        long totalUpdated = 0L;
        int page = 0;
        while (true) {
            Query q = new Query();
            if (since != null) q.addCriteria(Criteria.where("updatedAt").gte(since));
            if (until != null) q.addCriteria(Criteria.where("updatedAt").lt(until));
            q.with(PageRequest.of(page, BATCH, Sort.by(Sort.Direction.ASC, "updatedAt").and(Sort.by("_id"))));
            q.fields().include("commandNorm").include("count"); // projection: 필요한 필드만

            List<CommandUsage> usage = mongo.find(q, CommandUsage.class);
            if (usage.isEmpty()) break;

            BulkOperations bulk = mongo.bulkOps(BulkOperations.BulkMode.UNORDERED, LinuxCommand.class);
            usage.forEach(u -> {
                Query uq = Query.query(Criteria.where("commandNorm").is(u.getCommandNorm()));
                Update up = new Update().set("searchCount", u.getCount());
                bulk.updateOne(uq, up);
            });
            var res = bulk.execute();
            totalUpdated += res.getModifiedCount();

            page++;
        }
        return totalUpdated;
    }
}
