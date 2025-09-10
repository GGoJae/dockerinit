package com.dockerinit.features.application.dockerfile.domain;

public record Healthcheck(
        String cmd, String interval, String timeout, Integer retries, String startPeriod
) {
}
