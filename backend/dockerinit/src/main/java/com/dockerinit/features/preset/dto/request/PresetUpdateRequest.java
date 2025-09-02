package com.dockerinit.features.preset.dto.request;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.preset.dto.spec.PresetKindDTO;
import com.dockerinit.features.preset.dto.spec.RenderPolicyDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Set;

public record PresetUpdateRequest(
        String displayName,
        @Size(max=2000) String description,
        PresetKindDTO presetKind,
        @Size(max=20) Set<@NotBlank String> tags,
        @Min(1) @Max(99) Integer schemaVersion,
        RenderPolicyDTO renderPolicy,
        @Size(max=20) List<@Valid PresetArtifactRequest> artifacts,
        @Size(max=10) Set<FileType> defaultTargets,
        @Size(max=4000) String instructions,
        Boolean active,
        Boolean deprecated,
        String deprecationNote
) {
}
