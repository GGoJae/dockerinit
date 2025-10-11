package com.dockerinit.features.application.presetv2.compose.repository;

import com.dockerinit.features.application.presetv2.compose.domain.ComposePresetDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.Set;

public interface ComposePresetRepository extends MongoRepository<ComposePresetDocument, String> {

    Optional<ComposePresetDocument> findByMeta_Slug(String slug);

    boolean existsByMeta_Slug(String slug);

    Page<ComposePresetDocument> findByMeta_ActiveTrue(Pageable pageable);

    Page<ComposePresetDocument> findByMeta_ActiveTrueAndTagsIn(Set<String> tags, Pageable pageable);
}
