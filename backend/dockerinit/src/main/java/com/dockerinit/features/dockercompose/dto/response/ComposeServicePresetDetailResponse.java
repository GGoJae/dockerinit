package com.dockerinit.features.dockercompose.dto.response;

import com.dockerinit.features.dockercompose.dto.spec.CategoryDTO;
import com.dockerinit.features.dockercompose.dto.spec.ServiceSpecDTO;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record ComposeServicePresetDetailResponse(
        String id,
        String slug,
        String displayName,
        String description,
        CategoryDTO category,
        Set<String> tags,
        Integer schemaVersion,
        ServiceSpecDTO service,
        Map<String, String> suggestedEnvDefaults,
        Boolean active,
        Instant createdAt,
        Instant updatedAt,
        Long usedCount
) {
}
