package com.dockerinit.features.application.dockercompose.mapper;

import com.dockerinit.features.application.dockercompose.domain.Service;
import com.dockerinit.features.application.dockercompose.domain.composeCustom.*;
import com.dockerinit.features.application.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.application.dockercompose.dto.spec.BuildDTO;
import com.dockerinit.features.application.dockercompose.dto.spec.HealthcheckDTO;
import com.dockerinit.features.application.dockercompose.dto.spec.ServiceSpecDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComposePlanMapper {

    public static ComposePlan toPlan(ComposeRequestV1 request) {
        List<String> warnings = new ArrayList<>();
        String project = (request.projectName() == null || request.projectName().isBlank())
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
                            ServiceSpecDTO serviceDTO = s.service();
                            BuildDTO rb = serviceDTO.build();
                            Build build = (rb == null)
                                    ? null : new Build(rb.context(), rb.dockerfile(), rb.args());


                            HealthcheckDTO rhc = serviceDTO.healthcheck();
                            Healthcheck healthcheck = (rhc == null)
                                    ? null :
                                    Healthcheck.builder()
                                            .test(rhc.test()).interval(rhc.interval()).timeout(rhc.timeout())
                                            .retries(rhc.retries()).startPeriod(rhc.startPeriod()).build();


                            return Service.builder()
                                    .name(serviceDTO.name())
                                    .image(serviceDTO.image())
                                    .build(build)
                                    .command(serviceDTO.command())
                                    .environment(serviceDTO.environment())
                                    .envFile(serviceDTO.envFile())
                                    .ports(serviceDTO.ports())
                                    .volumes(serviceDTO.volumes())
                                    .dependsOn(serviceDTO.dependsOn())
                                    .restart(serviceDTO.restart())
                                    .healthcheck(healthcheck)
                                    .build();
                        }
                )
                .sorted(Comparator.comparing(Service::name))
                .toList();
    }

    private static <V, D> Map<String, D> sortedMapOptional(Map<String, V> src, Function<V, D> mapper) {
        if (src == null || src.isEmpty()) return Map.of();

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
