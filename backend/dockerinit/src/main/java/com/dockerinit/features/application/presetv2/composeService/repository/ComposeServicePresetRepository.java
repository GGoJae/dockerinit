package com.dockerinit.features.application.presetv2.composeService.repository;

import com.dockerinit.features.application.presetv2.composeService.domain.ComposeServicePresetDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.Set;

public interface ComposeServicePresetRepository extends MongoRepository<ComposeServicePresetDocument, String> {

    Optional<ComposeServicePresetDocument> findByMeta_Slug(String slug);

    boolean existsByMeta_Slug(String slug);

    Page<ComposeServicePresetDocument> findByMeta_ActiveTrue(Pageable pageable);

    Page<ComposeServicePresetDocument> findByMeta_ActiveTrueAndTagsIn(Set<String> tags, Pageable pageable);
}
