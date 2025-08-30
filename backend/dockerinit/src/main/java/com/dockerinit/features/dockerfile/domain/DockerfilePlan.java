package com.dockerinit.features.dockerfile.domain;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.EnvMode;

import java.util.*;

public record DockerfilePlan(
        String baseImage,
        String workdir,
        List<CopyEntry> copy,
        List<CopyEntry> add,
        EnvMode envMode,
        Map<String, String> envVars,
        List<Integer> expose,
        List<String> cmd,
        List<String> run,
        List<String> entrypoint,
        Map<String, String> label,
        String user,
        Map<String, String> args,
        Healthcheck healthcheck,
        List<String> volume,
        List<String> warnings,
        Set<FileType> targets
) {
    private static final String WARN_NO_ENTRY = "No CMD/ENTRYPOINT: container won't start without a command.";

    public DockerfilePlan {
        if (baseImage == null || baseImage.isBlank()) {
            throw new IllegalArgumentException("baseImage는 필수");
        }

        baseImage = baseImage.trim();
        copy = immutableOrEmpty(copy);
        add = immutableOrEmpty(add);
        envVars = immutableOrEmpty(envVars);
        expose = immutableOrEmpty(expose);
        cmd = immutableOrEmpty(cmd);
        run = immutableOrEmpty(run);
        entrypoint = immutableOrEmpty(entrypoint);
        label = immutableOrEmpty(label);
        args = immutableOrEmpty(args);
        volume = immutableOrEmpty(volume);

        List<String> w = (warnings == null) ? new ArrayList<>() : new ArrayList<>(warnings);
        if ((cmd.isEmpty()) && (entrypoint.isEmpty())) {
            w.add(WARN_NO_ENTRY);
        }
        warnings = List.copyOf(w);

        targets = (targets == null || targets.isEmpty())
                ? Set.of(FileType.DOCKERFILE)
                : Set.copyOf(targets);

        envMode = (envMode == null) ? EnvMode.DEV : envMode;

    }

    private static <T> List<T> immutableOrEmpty(List<T> target) {
        return (target == null) ? List.of() : List.copyOf(target);
    }

    private static <K, V> Map<K, V> immutableOrEmpty(Map<K, V> target) {
        if (target == null) return Map.of();

        for (Map.Entry<K, V> e : target.entrySet()) {
            if (e.getKey() == null) {
                throw new IllegalArgumentException("envVars/label/args: null key");
            }
            if (e.getValue() == null) {
                throw new IllegalArgumentException(
                        "envVars/label/args: null value for key '" + e.getKey() + "'"
                );
            }
        }
        return Map.copyOf(target);
    }
}
