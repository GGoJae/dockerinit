package com.dockerinit.features.preset.repository;

import com.dockerinit.features.preset.domain.PresetDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class PresetRepositoryImpl implements PresetRepositoryCustom{
    private final MongoTemplate mongo;

    @Override
    public void increaseDownloadCount(String id, long delta) {
        mongo.updateFirst(
                query(where("_id").is(id)),
                new Update().inc("downloadCount", delta),
                PresetDocument.class
        );
    }

    @Override
    public void touchUpdatedAt(String id, Instant now, String updateBy) {
        mongo.updateFirst(
                query(where("_id").is(id)),
                new Update().set("updatedAt", now).set("updatedBy", updateBy),
                PresetDocument.class
        );
    }
}
