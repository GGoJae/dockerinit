package com.dockerinit.features.application.dockercompose.dto.admin;

import com.dockerinit.features.application.dockercompose.dto.admin.spec.CategoryDTO;
import com.dockerinit.features.application.dockercompose.dto.admin.spec.ServiceDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.Map;
import java.util.Set;

public record ComposeServicePresetCreateRequest(
        @NotBlank
        @Pattern(regexp = "^[a-z0-9-]+$", message = "slug must be kebab-case")
        String slug,

        @NotBlank
        String displayName,

        @NotBlank
        String description,

        @NotNull
        CategoryDTO category,

        Set<@NotBlank String> tags,

        @NotNull
        @Positive Integer schemaVersion,

        @NotNull
        @Valid ServiceDTO service,

        Map<@NotBlank String, @NotBlank String> suggestedEnvDefaults,

        @NotNull
        Boolean active
) {
}
