package com.dockerinit.features.dockerfile.util;

import com.dockerinit.features.dockerfile.model.CopyEntry;
import com.dockerinit.features.dockerfile.model.DockerfileSpec;
import com.dockerinit.features.dockerfile.model.HealthcheckSpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DockerfileGenerator {

    public static String generate(DockerfileSpec spec) {
        StringBuilder sb = new StringBuilder(512);

        // 1) FROM
        sb.append("FROM ").append(spec.baseImage()).append('\n');

        // 2) LABEL (키 정렬)
        appendLabels(sb, spec.label());

        // 3) ARG (키 정렬)
        appendArgs(sb, spec.args());

        // 4) ENV (모드 기반 정책)
        appendEnvs(sb, spec.envVars(), spec.envMode());

        // 5) WORKDIR
        if (notBlank(spec.workdir())) {
            sb.append("WORKDIR ").append(spec.workdir()).append('\n');
        }

        // 6) ADD (JSON array 형식)
        appendCopyLike(sb, "ADD", spec.add());

        // 7) COPY (JSON array 형식)
        appendCopyLike(sb, "COPY", spec.copy());

        // 8) RUN (한 줄 하나씩)
        appendRun(sb, spec.run());

        // 9) USER
        if (notBlank(spec.user())) {
            sb.append("USER ").append(spec.user()).append('\n');
        }

        // 10) VOLUME (JSON array)
        appendVolume(sb, spec.volume());

        // 11) EXPOSE
        appendExpose(sb, spec.expose());

        // 12) HEALTHCHECK
        appendHealthcheck(sb, spec.healthcheck());

        // 13) ENTRYPOINT (JSON array)
        appendJsonArrayInstr(sb, "ENTRYPOINT", spec.entrypoint());

        // 14) CMD (JSON array)
        appendJsonArrayInstr(sb, "CMD", spec.cmd());

        // 마지막 개행/공백 정리
        return trimTrailingNewlines(sb.toString());
    }

    /* -------------------- helpers -------------------- */

    private static void appendLabels(StringBuilder sb, Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) return;
        labels.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append("LABEL ")
                        .append(e.getKey()).append('=')
                        .append(jsonQuote(e.getValue()))
                        .append('\n'));
    }

    private static void appendArgs(StringBuilder sb, Map<String, String> args) {
        if (args == null || args.isEmpty()) return;
        args.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    // ARG는 값이 없을 수도 있음
                    if (e.getValue() == null || e.getValue().isBlank()) {
                        sb.append("ARG ").append(e.getKey()).append('\n');
                    } else {
                        sb.append("ARG ").append(e.getKey()).append('=').append(e.getValue()).append('\n');
                    }
                });
    }

    private static void appendEnvs(StringBuilder sb, Map<String, String> envs, DockerfileSpec.EnvMode mode) {
        if (envs == null || envs.isEmpty()) return;

        // dev => 인라인, 그 외(staging/prod/null) => 런타임 주입 패턴
        boolean inline = (mode == DockerfileSpec.EnvMode.dev);

        if (!inline) {
            sb.append("# Prefer runtime-injected environment variables\n");
        }

        envs.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    String k = e.getKey();
                    String v = e.getValue();

                    if (inline) {
                        sb.append("ENV ").append(k).append('=').append(v).append('\n');
                    } else {
                        sb.append("# export ").append(k).append('=').append(v).append('\n');
                        sb.append("ENV ").append(k).append("=${").append(k).append('}').append('\n');
                    }
                });
    }

    private static void appendCopyLike(StringBuilder sb, String instr, List<CopyEntry> entries) {
        if (entries == null || entries.isEmpty()) return;
        for (CopyEntry e : entries) {
            // JSON array 형식: 공백/특수문자 안전
            sb.append(instr).append(' ')
                    .append(jsonArray(List.of(e.sourceRelPath(), e.targetAbsPath())))
                    .append('\n');
        }
    }

    private static void appendRun(StringBuilder sb, List<String> lines) {
        if (lines == null || lines.isEmpty()) return;
        // 필요하면 여기서 RUN 병합(&& \) 로직을 도입할 수 있음
        for (String line : lines) {
            sb.append("RUN ").append(line).append('\n');
        }
    }

    private static void appendVolume(StringBuilder sb, List<String> volumes) {
        if (volumes == null || volumes.isEmpty()) return;
        sb.append("VOLUME ").append(jsonArray(volumes)).append('\n');
    }

    private static void appendExpose(StringBuilder sb, List<Integer> ports) {
        if (ports == null || ports.isEmpty()) return;
        ports.stream()
                .sorted(Comparator.naturalOrder())
                .forEach(p -> sb.append("EXPOSE ").append(p).append('\n'));
    }

    private static void appendHealthcheck(StringBuilder sb, HealthcheckSpec h) {
        if (h == null || !notBlank(h.cmd())) return;

        List<String> opts = new ArrayList<>();
        if (notBlank(h.interval()))     opts.add("--interval=" + h.interval());
        if (notBlank(h.timeout()))      opts.add("--timeout=" + h.timeout());
        if (h.retries() != null)        opts.add("--retries=" + h.retries());
        if (notBlank(h.startPeriod()))  opts.add("--start-period=" + h.startPeriod());

        sb.append("HEALTHCHECK ");
        if (!opts.isEmpty()) {
            sb.append(String.join(" ", opts)).append(' ');
        }

        String cmd = h.cmd().trim();
        if (!cmd.regionMatches(true, 0, "CMD ", 0, 4)) {
            sb.append("CMD ").append(cmd).append('\n');
        } else {
            sb.append(cmd).append('\n');
        }
    }

    private static void appendJsonArrayInstr(StringBuilder sb, String instr, List<String> items) {
        if (items == null || items.isEmpty()) return;
        sb.append(instr).append(' ').append(jsonArray(items)).append('\n');
    }

    /* -------------------- tiny utils -------------------- */

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String trimTrailingNewlines(String s) {
        int i = s.length();
        while (i > 0 && (s.charAt(i - 1) == '\n' || s.charAt(i - 1) == '\r')) i--;
        return (i == s.length()) ? s : s.substring(0, i);
    }

    private static String jsonArray(List<String> parts) {
        if (parts == null || parts.isEmpty()) return "[]";
        return parts.stream().map(DockerfileGenerator::jsonQuote)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String jsonQuote(String s) {
        if (s == null) return "\"\"";
        // 최소한의 JSON escape
        String esc = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + esc + "\"";
    }
}
