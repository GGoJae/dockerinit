package com.dockerinit.features.application.dockercompose.repository;

import com.dockerinit.features.application.dockercompose.domain.composePreset.Category;
import com.dockerinit.features.application.dockercompose.domain.composePreset.ComposeServicePresetDocument;
import com.dockerinit.features.application.preset.domain.PresetDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class ComposeServicePresetRepositoryImpl implements ComposeServicePresetRepositoryCustom {

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

    @Override
    public Page<ComposeServicePresetDocument> search(Category category, Set<String> tags, boolean matchAll, Pageable pageable, boolean activeOnly) {
        List<Criteria> ands = new ArrayList<>();

        if (activeOnly) {
            ands.add(Criteria.where("active").is(true));
        }
        if (category != null) {
            ands.add(Criteria.where("category").is(category));
        }
        if (tags != null && !tags.isEmpty()) {
            ands.add(matchAll ?
                    Criteria.where("tags").all(tags) :
                    Criteria.where("tags").in(tags)
            );
        }

        Query base = new Query();
        if (!ands.isEmpty()) {
            base.addCriteria(new Criteria().andOperator(ands));
        }
        Query countQ = Query.of(base).limit(-1).skip(-1);
        Query contentQ = base.with(pageable);

        contentQ.fields()
                .include("id")
                .include("slug")
                .include("displayName")
                .include("category")
                .include("tags")
                .include("deprecated")
                .include("updatedAt")
                .include("usedCount");

        if (pageable.getSort().isUnsorted()) {
            contentQ.with(Sort.by(Sort.Direction.DESC, "updatedAt"));
        }

        List<ComposeServicePresetDocument> content =
                mongo.find(contentQ, ComposeServicePresetDocument.class);

        return PageableExecutionUtils.getPage(
                content, pageable,
                () -> mongo.count(countQ, ComposeServicePresetDocument.class)
        );
    }

}
