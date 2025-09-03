package com.dockerinit.features.dockercompose.repository;

import com.dockerinit.features.dockercompose.domain.composePreset.Category;
import com.dockerinit.features.dockercompose.domain.composePreset.ComposeServicePresetDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Optional;

public interface ComposeServicePresetRepository
    extends MongoRepository<ComposeServicePresetDocument, String>, ComposeServicePresetRepositoryCustom {

    Optional<ComposeServicePresetDocument> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<ComposeServicePresetDocument> findAllByActiveTrue(Pageable pageable);

    Page<ComposeServicePresetDocument> findAllByCategoryAndActiveTrue(Category category, Pageable pageable);

    Page<ComposeServicePresetDocument> findAllByTagsInAndActiveTrue(Collection<String> tags, Pageable pageable);
}
