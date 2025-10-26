package com.dockerinit.features.application.presetV2.dockerfile.repository;

import com.dockerinit.features.application.presetV2.dockerfile.domain.DockerfilePresetDocument;
import com.dockerinit.features.application.presetV2.dockerfile.dto.DockerfilePlanProjection;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetSuggestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DockerfilePresetRepository extends MongoRepository<DockerfilePresetDocument, String> {

    Optional<DockerfilePresetDocument> findByMeta_Slug(String slug);

    boolean existsByMeta_Slug(String slug);

    Page<DockerfilePresetDocument> findByMeta_ActiveTrue(Pageable pageable);

    Page<DockerfilePresetDocument> findByMeta_ActiveTrueAndMeta_TagsIn(Set<String> tags, Pageable pageable);

    @Aggregation(pipeline = {
            "{ $match: { 'meta.active': true } }",
            "{ $project: { _id: 0, slug: '$meta.slug', displayName: '$meta.displayName', " +
                    "             deprecated: '$meta.deprecated', tags: '$meta.tags', kind: 1 } }",
            "{ $sort: { 'meta.metrics.viewed': -1, 'updatedAt': -1 } }"
    })
    List<PresetSuggestDTO> suggestAll();

    @Query(
            value = "{ 'meta.slug': ?0 }",
            fields = "{ 'updatedAt': 1, 'version': 1, 'plan': 1 }")
    Optional<DockerfilePlanProjection> findPlanBySlug(String slug);

}
