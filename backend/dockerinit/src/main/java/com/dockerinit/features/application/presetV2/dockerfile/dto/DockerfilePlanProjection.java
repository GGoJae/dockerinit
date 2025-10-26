package com.dockerinit.features.application.presetV2.dockerfile.dto;

import com.dockerinit.features.application.dockerfile.domain.DockerfilePlan;

import java.time.Instant;

public interface DockerfilePlanProjection {
    Instant getUpdatedAt();
    Long getVersion();
    DockerfilePlan getPlan();
}
