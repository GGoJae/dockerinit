package com.dockerinit.features.dockercompose.repository;

import com.dockerinit.features.dockercompose.domain.composePreset.ComposeServicePresetDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class ComposeServicePresetRepositoryImpl implements ComposeServicePresetRepositoryCustom{

    private final MongoTemplate mongo;


    @Override
    public void increaseUsedCount(String id, long delta) {
        mongo.updateFirst(
                query(where("_id").is(id)),
                new Update().inc("usedCount", delta).currentDate("updatedAt"),
                ComposeServicePresetDocument.class
        );
    }
}
