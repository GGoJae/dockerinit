package com.dockerinit.features.dockerfile.render;

import com.dockerinit.features.dockerfile.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DefaultDockerfileRenderer implements DockerfileRenderer {

    private static final int DEFAULT_CAPACITY = 512;
    private static final Function<String, String> PLACEHOLDER =
            (s) -> {return "${" + s + "}";};
    private static final Pattern SECRET_KEY_PATTERN = Pattern.compile("(?i)(secret|password|passwd|token|apikey|api_key|credential)");

    @Override
    public RenderResult render(DockerfileSpec spec) {
        ArrayList<String> warnings = new ArrayList<>();
        boolean prodLike = spec.envMode() != EnvMode.DEV;

        String dockerfile = buildDockerfile(spec, prodLike, warnings);

        boolean hasSecret = looksSensitive(spec.envVars());
        boolean hasEnvFile = prodLike && spec.envVars() != null && !spec.envVars().isEmpty();   // TODO 요청에 envFile 필요한지 추가

        Map<String, String> envForEnvFile = hasEnvFile ? spec.envVars().keySet().stream().sorted()
                .collect(Collectors.toMap(k -> k, k -> "나를_바꿔주세요",
                        (a, b) -> a, LinkedHashMap::new)) : Map.of();

        ArrayList<GeneratedFile> extras = new ArrayList<>();
        // TODO README 나 .dockerignore 같은 파일 넣기

        return new RenderResult(
                dockerfile,
                spec.envMode(),
                hasEnvFile,
                Map.copyOf(envForEnvFile),
                List.copyOf(extras),
                List.copyOf(warnings),
                hasSecret);
    }

    private boolean looksSensitive(Map<String, String> envs) {
        if (Objects.isNull(envs)) return false;

        return envs.keySet().stream().anyMatch(k -> SECRET_KEY_PATTERN.matcher(k).find());
    }

    private static String buildDockerfile(DockerfileSpec spec, boolean prodLike, List<String> warnings) {
        // TODO warnings 에 위험부분 추가하는 로직 작성
        StringBuilder sb = new StringBuilder(DEFAULT_CAPACITY);

        // 1) FROM
        sb.append("FROM ").append(spec.baseImage()).append('\n');

        // 2) LABEL (키 정렬)
        appendLabels(sb, spec.label());

        // 3) ARG (키 정렬)
        appendArgs(sb, spec.args());

        // 4) ENV (모드 기반 정책)
        appendEnvs(sb, spec.envVars(), prodLike);

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

    private static void appendEnvs(StringBuilder sb, Map<String, String> envs, boolean prodLike) {
        if (envs == null || envs.isEmpty()) return;


        if (prodLike) {
            sb.append("# Prefer runtime-injected environment variables\n");
        }

        envs.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    String k = e.getKey();
                    String v = e.getValue();

                    if (prodLike) {
                        sb.append("ENV ").append(k).append("=").append(PLACEHOLDER.apply(k)).append('\n');
                    } else {
                        sb.append("ENV ").append(k).append('=').append(jsonQuote(v)).append('\n');
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
        return parts.stream().map(DefaultDockerfileRenderer::jsonQuote)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String jsonQuote(String s) {
        if (s == null) return "\"\"";
        // 최소한의 JSON escape
        String esc = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + esc + "\"";
    }
}
