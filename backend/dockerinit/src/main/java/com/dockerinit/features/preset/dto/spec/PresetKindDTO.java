package com.dockerinit.features.preset.dto.spec;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PresetKindDTO {
    DOCKERFILE, COMPOSE, BUNDLE,
    @JsonEnumDefaultValue UNKNOWN
}
