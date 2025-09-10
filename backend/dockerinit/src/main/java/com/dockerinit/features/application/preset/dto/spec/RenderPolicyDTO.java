package com.dockerinit.features.application.preset.dto.spec;

public record RenderPolicyDTO(
        EnvValueModeDTO envValueMode,
        String placeholderFormat,
        Boolean includeManifestByDefault,
        Boolean ensureTrailingNewline,
        String lineEndings
) {
}
