package com.dockerinit.features.dockercompose.dto.spec;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum CategoryDTO {
    LANGUAGE, DATABASE, CACHE, QUEUE, OBSERVABILITY, RUNTIME, FRAMEWORK, OTHER,
    @JsonEnumDefaultValue UNKNOWN
}
