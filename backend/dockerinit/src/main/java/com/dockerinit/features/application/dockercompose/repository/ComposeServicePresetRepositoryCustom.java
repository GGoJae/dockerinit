package com.dockerinit.features.application.dockercompose.repository;

import com.dockerinit.features.application.dockercompose.domain.composePreset.Category;
import com.dockerinit.features.application.dockercompose.domain.composePreset.ComposeServicePresetDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ComposeServicePresetRepositoryCustom {
    void increaseUsedCount(String id, long delta);

    void increaseUsedCountBySlug(String slug, long delta);

    Page<ComposeServicePresetDocument> search(
            Category category,
            Set<String> tags,
            boolean matchAll,
            Pageable pageable,
            boolean activeOnly
    );
}
