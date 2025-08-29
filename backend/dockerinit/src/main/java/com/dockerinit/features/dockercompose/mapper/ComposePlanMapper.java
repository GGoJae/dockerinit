package com.dockerinit.features.dockercompose.mapper;

import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.dockercompose.domain.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComposePlanMapper {

    public static ComposePlan toPlan(ComposeRequestV1 request) {
        List<String> warnings = new ArrayList<>();
        String project = (Objects.isNull(request.projectName()) || request.projectName().isBlank())
                ? "app" : request.projectName();

        List<Service> services = mapToServices(request);

        Map<String, Network> networks = sortedMapOptional(request.networks(), (rn) -> new Network(rn.driver()));
        Map<String, Volume> volumes = sortedMapOptional(request.volumes(), (rv) -> new Volume(rv.driver()));

        return ComposePlan.builder()
                .projectName(project)
                .services(services)
                .networks(networks)
                .volumes(volumes)
                .build();
    }

    private static List<Service> mapToServices(ComposeRequestV1 request) {
        return Optional.ofNullable(request.services()).orElseGet(() -> List.of())
                .stream().map(
                        s -> {
                            ComposeRequestV1.Build rb = s.build();
                            Build build = Objects.isNull(rb)
                                    ? null : new Build(rb.context(), rb.dockerfile(), rb.args());


                            ComposeRequestV1.Healthcheck rhc = s.healthcheck();
                            Healthcheck healthcheck = Objects.isNull(rhc)
                                    ? null :
                                    Healthcheck.builder()
                                            .test(rhc.test()).interval(rhc.interval()).timeout(rhc.timeout())
                                            .retries(rhc.retries()).startPeriod(rhc.startPeriod()).build();


                            return Service.builder()
                                    .name(s.name())
                                    .image(s.image())
                                    .build(build)
                                    .command(s.command())
                                    .enviroment(s.environment())
                                    .envFile(s.envFile())
                                    .ports(s.ports())
                                    .volumes(s.volumes())
                                    .dependsOn(s.dependsOn())
                                    .restart(s.restart())
                                    .healthcheck(healthcheck)
                                    .build();
                        }
                )
                .sorted(Comparator.comparing(Service::name))
                .toList();
    }

    private static <V, D> Map<String, D> sortedMapOptional(Map<String, V> src, Function<V, D> mapper) {
        if (Objects.isNull(src) || src.isEmpty()) return Map.of();

        return src.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> mapper.apply(e.getValue()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
