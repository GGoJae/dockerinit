package com.dockerinit.features.application.presetV2.compose.materializer;

import com.dockerinit.features.application.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.application.presetV2.compose.domain.ComposePresetDocument;
import com.dockerinit.features.application.presetV2.compose.repository.ComposePresetRepository;
import com.dockerinit.global.exception.IllegalArgumentCustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComposePresetMaterializer {

    private final ComposePresetRepository repo;

    /** slug 기준 프리셋 payload(ComposePlan) 반환 */
    public ComposePlan toPlan(String slug) {
        ComposePresetDocument doc = repo.findByMeta_Slug(slug)
                .orElseThrow(() -> new IllegalArgumentCustomException("compose preset not found: " + slug));
        if (doc.getPlan() == null) {
            throw new IllegalArgumentCustomException("compose preset has no plan: " + slug);
        }
        return doc.getPlan();
    }
}
