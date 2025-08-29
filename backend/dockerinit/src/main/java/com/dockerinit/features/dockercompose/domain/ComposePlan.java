package com.dockerinit.features.dockercompose.domain;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Builder
public record ComposePlan(
        String projectName,
        List<Service> services,
        Map<String, Network> networks,
        Map<String, Volume> volumes,
        List<String> warnings
) {
    public ComposePlan {
        projectName = (Objects.isNull(projectName) || projectName.isBlank()) ? "app" : projectName.trim();
        services = Objects.isNull(services) ? List.of() : List.copyOf(services);
        networks = Objects.isNull(networks) ? Map.of() : Map.copyOf(networks);
        volumes = Objects.isNull(volumes) ? Map.of() : Map.copyOf(volumes);

        List<String> w = Objects.isNull(warnings) ? new ArrayList<>() : new ArrayList<>(warnings);
        if (services.isEmpty()) w.add("서비스가 존재하지 않습니다");

        warnings = List.copyOf(w);
    }
}
