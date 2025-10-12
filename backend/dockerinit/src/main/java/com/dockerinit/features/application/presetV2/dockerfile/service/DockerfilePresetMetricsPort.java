package com.dockerinit.features.application.presetV2.dockerfile.service;

import com.dockerinit.features.application.presetV2.dockerfile.domain.DockerfilePresetDocument;
import com.dockerinit.features.application.presetV2.shared.domain.PresetEventType;
import com.dockerinit.features.application.presetV2.shared.domain.PresetKind;
import com.dockerinit.features.application.presetV2.shared.service.MetricsFieldPathResolver;
import com.dockerinit.features.application.presetV2.shared.service.spi.PresetMetricPort;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DockerfilePresetMetricsPort implements PresetMetricPort {

    private final MongoTemplate template;

    @Override
    public PresetKind kind() {
        return PresetKind.DOCKERFILE;
    }

    @Override
    public boolean increment(String slug, PresetEventType event) {
        Query q = Query.query(Criteria.where("meta.slug").is(slug));
        Update u = new Update().inc(MetricsFieldPathResolver.path(event), 1);
        UpdateResult res = template.updateFirst(q, u, DockerfilePresetDocument.class);
        return res.getMatchedCount() > 0;
    }
}
