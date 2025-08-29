package com.dockerinit.features.dockercompose.domain;

import lombok.Builder;

@Builder
public record Healthcheck(
        String test,
        String interval,
        String timeout,
        Integer retries,
        String startPeriod
) {
}
