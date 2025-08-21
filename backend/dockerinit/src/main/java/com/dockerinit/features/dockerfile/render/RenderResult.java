package com.dockerinit.features.dockerfile.model;

import java.util.List;
import java.util.Map;

public record RenderResult(
        String dockerfile,
        EnvMode envMode,
        boolean hasEnvFile,
        Map<String, String> envForEnvFile,
        List<GeneratedFile> extras,
        List<String> warnings,
        boolean containsSecrets
) {}
