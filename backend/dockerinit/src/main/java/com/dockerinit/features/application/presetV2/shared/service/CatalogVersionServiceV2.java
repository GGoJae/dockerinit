package com.dockerinit.features.application.presetV2.shared.service;

import com.dockerinit.features.application.presetV2.shared.domain.CatalogVersionDocument;
import com.dockerinit.features.application.presetV2.shared.domain.PresetKind;
import com.dockerinit.features.application.presetV2.shared.repository.CatalogVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CatalogVersionServiceV2 {

    private final CatalogVersionRepository repo;

    private String key(PresetKind kind) {
        return "preset:" + kind.name().toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public long get(PresetKind kind) {
        return repo.findById(key(kind))
                .map(CatalogVersionDocument::getValue)
                .orElse(0L);
    }

    @Transactional
    public long bump(PresetKind kind) {
        String id = key(kind);
        CatalogVersionDocument doc = repo.findById(id)
                .orElseGet(() -> CatalogVersionDocument.builder().id(id).value(0L).build());
        doc = doc.toBuilder().value(doc.getValue() + 1L).build();
        return repo.save(doc).getValue();
    }
}
