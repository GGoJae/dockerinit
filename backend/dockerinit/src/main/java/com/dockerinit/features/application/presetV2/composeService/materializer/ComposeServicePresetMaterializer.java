package com.dockerinit.features.application.presetV2.composeService.materializer;

import com.dockerinit.features.application.dockercompose.domain.model.Service;
import com.dockerinit.features.application.presetV2.composeService.domain.ComposeServicePresetDocument;
import com.dockerinit.features.application.presetV2.composeService.repository.ComposeServicePresetRepositoryV2;
import com.dockerinit.global.exception.IllegalArgumentCustomException;
import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ComposeServicePresetMaterializer {

    private final ComposeServicePresetRepositoryV2 repo;

    /** slug 기준 서비스 조각 반환 */
    public Service toService(String slug) {
        ComposeServicePresetDocument doc = repo.findByMeta_Slug(slug)
                .orElseThrow(() -> new IllegalArgumentCustomException("compose-service preset not found: " + slug));
        if (doc.getService() == null) {
            throw new IllegalArgumentCustomException("compose-service preset has no service payload: " + slug);
        }
        return doc.getService();
    }
}
