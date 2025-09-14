package com.dockerinit.features.application.dockercompose.dto.admin;

import com.dockerinit.features.application.dockercompose.dto.admin.spec.ServiceDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.Map;
import java.util.Set;

public record ComposeServicePresetUpdateRequest(
        String displayName,

        String description,

        String category,

        Set<@NotBlank String> tags,

        @Positive
        Integer schemaVersion,

        @Valid
        ServiceDTO service,

        Map<@NotBlank String, @NotBlank String> suggestedEnvDefaults,

        Boolean active
) {
}
