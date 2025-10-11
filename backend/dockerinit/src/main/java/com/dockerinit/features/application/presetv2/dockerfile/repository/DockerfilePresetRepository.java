package com.dockerinit.features.application.presetv2.dockerfile.repository;

import com.dockerinit.features.application.presetv2.dockerfile.domain.DockerfilePresetDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.Set;

public interface DockerfilePresetRepository extends MongoRepository<DockerfilePresetDocument, String> {

    Optional<DockerfilePresetDocument> findByMeta_Slug(String slug);

    boolean existsByMeta_Slug(String slug);

    Page<DockerfilePresetDocument> findByMeta_ActiveTrue(Pageable pageable);

    Page<DockerfilePresetDocument> findByMeta_ActiveTrueAndTagsIn(Set<String> tags, Pageable pageable);

}
