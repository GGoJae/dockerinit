package com.dockerinit.features.dockerfile.domain;

public record Healthcheck(
        String cmd, String interval, String timeout, Integer retries, String startPeriod
) {
}
