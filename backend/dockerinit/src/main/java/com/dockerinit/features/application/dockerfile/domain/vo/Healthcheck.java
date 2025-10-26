package com.dockerinit.features.application.dockerfile.domain.vo;

public record Healthcheck(
        String cmd, String interval, String timeout, Integer retries, String startPeriod
) {
}
