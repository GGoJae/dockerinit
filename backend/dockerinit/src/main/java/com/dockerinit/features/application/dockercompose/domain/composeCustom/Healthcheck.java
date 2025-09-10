package com.dockerinit.features.application.dockercompose.domain.composeCustom;

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
