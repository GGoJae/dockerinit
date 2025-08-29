package com.dockerinit.features.dockercompose.domain;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Builder
public record Service(
        String name,
        String image,
        Build build,
        List<String> command,
        Map<String, String> enviroment,
        List<String> envFile,
        List<String> ports,
        List<String> volumes,
        List<String> dependsOn,
        String restart,
        Healthcheck healthcheck
) {
    public Service {
        if (Objects.isNull(name) || name.isBlank()) throw new IllegalArgumentException("서비스 이름은 필수입니다");
        command = immutableOrEmpty(command);
        enviroment = immutableOrEmpty(enviroment);
        envFile = immutableOrEmpty(envFile);
        ports = immutableOrEmpty(ports);
        volumes = immutableOrEmpty(volumes);
        dependsOn = immutableOrEmpty(dependsOn);
    }

    private static <T> List<T> immutableOrEmpty(List<T> target) {
        return target == null ? List.of() : List.copyOf(target);
    }

    private static <K, V> Map<K, V> immutableOrEmpty(Map<K, V> target) {
        return target == null ? Map.of() : Map.copyOf(target);
    }
}
