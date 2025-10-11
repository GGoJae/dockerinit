package com.dockerinit.features.application.presetv2.shared.repository;

import com.dockerinit.features.application.presetv2.shared.domain.CatalogVersionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CatalogVersionRepository extends MongoRepository<CatalogVersionDocument, String> {
}
