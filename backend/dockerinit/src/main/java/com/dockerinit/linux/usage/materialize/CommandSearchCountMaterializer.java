package com.dockerinit.linux.usage.materialize;

import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.usage.domain.CommandUsage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile({"local", "dev", "staging", "prod"})
public class CommandSearchCountMaterializer {

    private final MongoTemplate mongo;

    public void materialize() {
        int page = 0, size = 1000;
        while (true) {
            List<CommandUsage> usage = mongo.find(
                    new Query().with(PageRequest.of(page, size)),
                    CommandUsage.class
            );
            if (usage.isEmpty()) break;

            BulkOperations bulk = mongo.bulkOps(BulkOperations.BulkMode.UNORDERED, LinuxCommand.class);
            Instant now = Instant.now();
            usage.forEach(u -> {
                Query q = Query.query(Criteria.where("commandNorm").is(u.getCommandNorm()));
                Update up = new Update()
                        .set("searchCount", u.getCount())
                        .set("updatedAt", now); // 원하면 제거
                bulk.updateOne(q, up);
            });
            bulk.execute();

            page++;
        }
    }
}
