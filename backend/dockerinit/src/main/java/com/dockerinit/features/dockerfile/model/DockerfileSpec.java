package com.dockerinit.features.dockerfile.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record DockerfileSpec(
        String baseImage,
        String workdir,                          // null 허용 -> generator에서 처리
        List<CopyEntry> copy,
        List<CopyEntry> add,
        EnvMode envMode,
        Map<String, String> envVars,
        List<Integer> expose,
        List<String> cmd,
        List<String> run,
        List<String> entrypoint,
        Map<String, String> label,
        String user,
        Map<String, String> args,
        HealthcheckSpec healthcheck,
        List<String> volume
) {
    public enum EnvMode { dev, staging, prod }

    public DockerfileSpec {
        // 도메인 불변식(필수 최소 보장)
        if ((cmd == null || cmd.isEmpty()) && (entrypoint == null || entrypoint.isEmpty())) {
            throw new IllegalArgumentException("CMD 또는 ENTRYPOINT 중 하나는 필수");
        }
        // 그 외 도메인 레벨의 가벼운 체크들…
    }
}

