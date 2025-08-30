package com.dockerinit.features.dockerfile.mapper;

import com.dockerinit.features.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.dockerfile.dto.request.spec.AdditionalFile;
import com.dockerinit.features.dockerfile.dto.request.spec.CopyDirective;
import com.dockerinit.features.dockerfile.dto.request.spec.Mode;
import com.dockerinit.features.dockerfile.domain.CopyEntry;
import com.dockerinit.features.model.EnvMode;
import com.dockerinit.features.dockerfile.domain.Healthcheck;
import com.dockerinit.features.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.model.FileType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DockerfilePlanMapper {
    public static DockerfilePlan toPlan(DockerfileRequest req) {
        ArrayList<String> warnings = new ArrayList<>();

        // 1) targets 확정: DOCKERFILE은 항상 포함
        Set<FileType> targets = EnumSet.of(FileType.DOCKERFILE);
        if (req.additionalFiles() != null) {
            for (AdditionalFile af : req.additionalFiles()) {
                switch (af) {
                    case ENV -> targets.add(FileType.ENV);
                    case README -> targets.add(FileType.README);
                    case MANIFEST -> targets.add(FileType.MANIFEST);
                }
            }
        }

        // 2) 간단한 정책성 경고
        if (targets.contains(FileType.ENV) && (req.envVars() == null || req.envVars().isEmpty())) {
            warnings.add("ENV 파일이 요청되었지만 envVars가 비어있습니다. 비어있는 .env가 생성될 수 있습니다.");
        }
        if (targets.contains(FileType.README) && (req.baseImage() == null || req.baseImage().isBlank())) {
            warnings.add("README에 표기할 baseImage가 비어 있습니다.");
        }

        // TODO 빌더로 바꾸기
        return new DockerfilePlan(
                req.baseImage(),
                req.workdir(),
                toEntries(req.copy()),
                toEntries(req.add()),
                mapEnvMode(req.envMode()),
                safeMap(req.envVars()),
                safeList(req.expose()),
                safeList(req.cmd()),
                safeList(req.run()),
                safeList(req.entrypoint()),
                safeMap(req.label()),
                req.user(),
                safeMap(req.args()),
                req.healthcheck() == null ? null : new Healthcheck(
                        req.healthcheck().cmd(),
                        req.healthcheck().interval(),
                        req.healthcheck().timeout(),
                        req.healthcheck().retries(),
                        req.healthcheck().startPeriod()
                ),
                safeList(req.volume()),
                warnings,
                targets
        );
    }

    private static List<CopyEntry> toEntries(List<CopyDirective> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(cd -> new CopyEntry(cd.source(), cd.target()))
                .collect(Collectors.toList());
    }

    private static <T> List<T> safeList(List<T> l) { return l == null ? List.of() : List.copyOf(l); }
    private static <K,V> Map<K,V> safeMap(Map<K,V> m) { return m == null ? Map.of() : Map.copyOf(m); }

    private static EnvMode mapEnvMode(Mode m) {
        if (m == null) return null;
        return switch (m) {
            case dev -> EnvMode.DEV;
            case staging, prod -> EnvMode.PROD_LIKE;
        };
    }

}
