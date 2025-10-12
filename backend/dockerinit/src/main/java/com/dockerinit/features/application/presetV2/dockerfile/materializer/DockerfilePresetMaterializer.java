package com.dockerinit.features.application.presetV2.dockerfile.materializer;
// path: src/main/java/com/dockerinit/features/application/preset/dockerfile/materializer/DockerfilePresetMaterializer.java

import com.dockerinit.features.application.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.application.presetV2.dockerfile.domain.DockerfilePresetDocument;
import com.dockerinit.features.application.presetV2.dockerfile.repository.DockerfilePresetRepository;
import com.dockerinit.global.exception.IllegalArgumentCustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DockerfilePresetMaterializer {

    private final DockerfilePresetRepository repo;

    /** slug 기준 프리셋 payload(DockerfilePlan) 반환 */
    public DockerfilePlan toPlan(String slug) {
        DockerfilePresetDocument doc = repo.findByMeta_Slug(slug)
                .orElseThrow(() -> new IllegalArgumentCustomException("dockerfile preset not found: " + slug));
        if (doc.getPlan() == null) {
            throw new IllegalArgumentCustomException("dockerfile preset has no plan: " + slug);
        }
        return doc.getPlan();
    }
}
