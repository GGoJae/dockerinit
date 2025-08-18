package com.dockerinit.features.dockerfile.model;

public record HealthcheckSpec(
        String cmd, String interval, String timeout, Integer retries, String startPeriod
) {
}
