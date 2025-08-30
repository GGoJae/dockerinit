package com.dockerinit.features.dockercompose.domain;

import java.util.Map;
import java.util.Objects;

public record Build(
        String context,
        String dockerfile,
        Map<String, String> args
) {
    public Build {
        if (context == null || context.isBlank()) throw new IllegalArgumentException("build.context 는 필수입니다");
        args = (args == null) ? Map.of() : Map.copyOf(args);
    }
}
