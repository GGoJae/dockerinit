package com.dockerinit.features.application.dockercompose.domain.composeCustom;

import com.dockerinit.features.application.dockercompose.domain.Service;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Builder
public record ComposePlan(
        String projectName,
        List<Service> services,
        Map<String, Network> networks,
        Map<String, Volume> volumes,
        List<String> warnings
) {
    public ComposePlan {
        projectName = (projectName == null || projectName.isBlank()) ? "app" : projectName.trim();
        services = (services == null) ? List.of() : List.copyOf(services);
        networks = (networks == null) ? Map.of() : Map.copyOf(networks);
        volumes = (volumes == null) ? Map.of() : Map.copyOf(volumes);

        List<String> w = (warnings == null) ? new ArrayList<>() : new ArrayList<>(warnings);
        if (services.isEmpty()) w.add("서비스가 존재하지 않습니다");

        warnings = List.copyOf(w);
    }
}
