package com.dockerinit.features.application.dockercompose.repository;

import com.dockerinit.features.application.dockercompose.domain.composePreset.ComposeServicePresetDocument;
import com.dockerinit.features.application.preset.domain.PresetDocument;
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
                new Update().inc("usedCount", delta),
                ComposeServicePresetDocument.class
        );
    }

    @Override
    public void increaseUsedCountBySlug(String slug, long delta) {
        mongo.updateFirst(
                query(where("slug").is(slug)),
                new Update().inc("usedCount", delta),
                PresetDocument.class
        );
    }
}
