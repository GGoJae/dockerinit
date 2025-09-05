package com.dockerinit.features.preset.dto.spec;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PresetKindDTO {
    DOCKERFILE("df"), COMPOSE("cmp"), BUNDLE("bndl"),
    @JsonEnumDefaultValue UNKNOWN("known");

    private final String code;

    PresetKindDTO(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
