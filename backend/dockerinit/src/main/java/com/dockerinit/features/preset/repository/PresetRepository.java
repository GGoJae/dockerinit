package com.dockerinit.features.preset.repository;

import com.dockerinit.features.preset.domain.PresetDocument;
import com.dockerinit.features.preset.domain.PresetKind;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Optional;

public interface PresetRepository extends MongoRepository<PresetDocument, String>, PresetRepositoryCustom {

    Optional<PresetDocument> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<PresetDocument> findAllByPresetKindAndActiveTrue(PresetKind presetKind, Pageable pageable);

    Page<PresetDocument> findAllByTagsInAndActiveTrue(Collection<String> tags, Pageable pageable);
}
