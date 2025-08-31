package com.dockerinit.features.preset.dto.response;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.preset.dto.spec.PresetKindDTO;
import com.dockerinit.features.preset.dto.spec.RenderPolicyDTO;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Builder
public record PresetDetailResponse(
        String id,
        String slug,
        String displayName,
        String description,
        PresetKindDTO presetKind,
        Set<String> tags,
        Integer schemaVersion,
        RenderPolicyDTO renderPolicy,
        Set<FileType> defaultTargets,
        String instructions,
        Boolean active,
        Boolean deprecated,
        String deprecationNote,
        Instant createdAt,
        Instant updatedAt,
        Long downloadCount,
        List<PresetArtifactMetaResponse> artifacts

) {
}
