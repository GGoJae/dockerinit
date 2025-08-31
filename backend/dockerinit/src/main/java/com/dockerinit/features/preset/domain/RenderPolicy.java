package com.dockerinit.features.preset.domain;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class RenderPolicy {
    private EnvValueMode envValueMode;
    private String placeholderFormart;
    private Boolean includeManifestByDefault;
    private Boolean ensureTrailingNewline;
    private String lineEndings;
}
