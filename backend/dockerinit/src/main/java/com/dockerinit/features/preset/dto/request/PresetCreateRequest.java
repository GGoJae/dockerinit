package com.dockerinit.features.preset.dto.request;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.preset.dto.spec.PresetKindDTO;
import com.dockerinit.features.preset.dto.spec.RenderPolicyDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Set;

public record PresetCreateRequest(
        @NotBlank @Pattern(regexp="^[a-z0-9]([a-z0-9-]*[a-z0-9])?$", message="소문자/숫자/하이픈 허용, 하이픈으로 시작/끝 불가")
        String slug,

        @NotBlank String displayName,
        @Size(max=2000) String description,

        @NotNull PresetKindDTO presetKind,

        @Size(max=20) Set<@NotBlank String> tags,

        @Min(1) @Max(99) Integer schemaVersion,

        RenderPolicyDTO renderPolicy,

        @NotNull @Size(min=1, max=20) List<@Valid PresetArtifactRequest> artifacts,

        @Size(max=10) Set<FileType> defaultTargets,

        @Size(max=4000) String instructions,

        @NotNull Boolean active,
        Boolean deprecated,
        String deprecationNote
) {}
