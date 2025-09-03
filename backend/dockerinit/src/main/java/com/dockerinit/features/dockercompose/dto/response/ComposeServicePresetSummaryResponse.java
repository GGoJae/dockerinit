package com.dockerinit.features.dockercompose.dto.response;

import com.dockerinit.features.dockercompose.dto.spec.CategoryDTO;

import java.time.Instant;
import java.util.Set;

public record ComposeServicePresetSummaryResponse(
        String id,
        String slug,
        String displayName,
        CategoryDTO category,
        Set<String> tags,
        Instant updatedAt,
        Long usedCount
) {
}
