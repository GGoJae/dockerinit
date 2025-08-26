package com.dockerinit.features.dockerfile.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record DockerfilePlan(
        String baseImage,
        String workdir,
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
        List<String> volume,
        List<String> warnings,
        Set<FileType> targets
) {
    private static final String WARN_NO_ENTRY = "No CMD/ENTRYPOINT: container won't start without a command.";

    public DockerfilePlan {
        // 1) 널 방어 + 복사 (외부 리스트/셋과 분리)
        copy       = copy       == null ? List.of() : List.copyOf(copy);
        add        = add        == null ? List.of() : List.copyOf(add);
        envVars    = envVars    == null ? Map.of()  : Map.copyOf(envVars);
        expose     = expose     == null ? List.of() : List.copyOf(expose);
        cmd        = cmd        == null ? List.of() : List.copyOf(cmd);
        run        = run        == null ? List.of() : List.copyOf(run);
        entrypoint = entrypoint == null ? List.of() : List.copyOf(entrypoint);
        label      = label      == null ? Map.of()  : Map.copyOf(label);
        args       = args       == null ? Map.of()  : Map.copyOf(args);
        volume     = volume     == null ? List.of() : List.copyOf(volume);

        // 2) warnings는 가변으로 받은 뒤 메시지 추가하고 불변화
        List<String> w = (warnings == null) ? new ArrayList<>() : new ArrayList<>(warnings);
        if ((cmd.isEmpty()) && (entrypoint.isEmpty())) {
            w.add(WARN_NO_ENTRY);
        }
        warnings = List.copyOf(w);

        // 3) targets 기본값/불변화
        targets = (targets == null || targets.isEmpty())
                ? Set.of(FileType.DOCKERFILE)
                : Set.copyOf(targets);

        // 4) envMode 기본값
        envMode = (envMode == null) ? EnvMode.DEV : envMode;

        // ※ 여기선 "진짜 스펙 위반"만 throw 고려 (e.g., baseImage 빈값)
        if (baseImage == null || baseImage.isBlank()) {
            throw new IllegalArgumentException("baseImage는 필수");
        }
    }
}
