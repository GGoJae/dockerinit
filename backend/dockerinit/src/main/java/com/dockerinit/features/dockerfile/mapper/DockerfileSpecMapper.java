package com.dockerinit.features.dockerfile.mapper;

import com.dockerinit.features.dockerfile.dto.DockerfileRequest;
import com.dockerinit.features.dockerfile.model.CopyEntry;
import com.dockerinit.features.dockerfile.model.DockerfileSpec;
import com.dockerinit.features.dockerfile.model.EnvMode;
import com.dockerinit.features.dockerfile.model.HealthcheckSpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DockerfileSpecMapper {
    public static DockerfileSpec toSpec(DockerfileRequest r) {
        return new DockerfileSpec(
                r.baseImage(),
                r.workdir(),
                toEntries(r.copy()),
                toEntries(r.add()),
                mapEnvMode(r.envMode()),
                safeMap(r.envVars()),
                safeList(r.expose()),
                safeList(r.cmd()),
                safeList(r.run()),
                safeList(r.entrypoint()),
                safeMap(r.label()),
                r.user(),
                safeMap(r.args()),
                r.healthcheck() == null ? null : new HealthcheckSpec(
                        r.healthcheck().cmd(),
                        r.healthcheck().interval(),
                        r.healthcheck().timeout(),
                        r.healthcheck().retries(),
                        r.healthcheck().startPeriod()
                ),
                safeList(r.volume())
        );
    }

    private static List<CopyEntry> toEntries(List<DockerfileRequest.CopyDirective> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(cd -> new CopyEntry(cd.source(), cd.target()))
                .collect(Collectors.toList());
    }

    private static <T> List<T> safeList(List<T> l) { return l == null ? List.of() : List.copyOf(l); }
    private static <K,V> Map<K,V> safeMap(Map<K,V> m) { return m == null ? Map.of() : Map.copyOf(m); }

    private static EnvMode mapEnvMode(DockerfileRequest.EnvModeDTO m) {
        if (m == null) return null;
        return switch (m) {
            case dev -> EnvMode.DEV;
            case staging -> EnvMode.STAGING;
            case prod -> EnvMode.PROD;
        };
    }

}
