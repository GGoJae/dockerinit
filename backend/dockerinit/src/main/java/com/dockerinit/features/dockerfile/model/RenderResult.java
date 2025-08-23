package com.dockerinit.features.dockerfile.model;

import java.util.List;

public record RenderResult(
        String dockerfile,
        List<String> warnings
) {}
