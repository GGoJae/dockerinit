package com.dockerinit.features.dockerfile.renderer.impl;

import com.dockerinit.features.dockerfile.model.CopyEntry;
import com.dockerinit.features.dockerfile.model.EnvMode;
import com.dockerinit.features.dockerfile.model.HealthcheckSpec;
import com.dockerinit.features.dockerfile.model.RenderContext;
import com.dockerinit.features.dockerfile.model.DockerfilePlan;
import com.dockerinit.features.dockerfile.renderer.ArtifactRenderer;
import com.dockerinit.features.dockerfile.model.FileType;
import com.dockerinit.features.dockerfile.model.GeneratedFile;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(1)
public class DockerfileRenderer implements ArtifactRenderer {

    private static final int DEFAULT_CAPACITY = 512;
    private static final Function<String, String> PLACEHOLDER =
            (s) -> {
                return "${" + s + "}";
            };
    public static final String DOCKERFILE = "Dockerfile";

    @Override
    public boolean supports(RenderContext ctx) {
        return true;
    }

    @Override
    public FileType fileType() {
        return FileType.DOCKERFILE;
    }


    @Override
    public List<GeneratedFile> render(RenderContext ctx, List<String> warnings) {
        DockerfilePlan plan = ctx.plan();
        boolean prodLike = plan.envMode() == EnvMode.PROD_LIKE;
        StringBuilder sb = new StringBuilder(DEFAULT_CAPACITY);

        // 1) FROM
        sb.append("FROM ").append(plan.baseImage()).append('\n');

        // 2) LABEL (키 정렬)
        appendLabels(sb, plan.label());

        // 3) ARG (키 정렬)
        appendArgs(sb, plan.args());

        // 4) ENV (모드 기반 정책)
        appendEnvs(sb, plan.envVars(), prodLike);

        // 5) WORKDIR
        if (notBlank(plan.workdir())) {
            sb.append("WORKDIR ").append(plan.workdir()).append('\n');
        }

        // 6) ADD (JSON array 형식)
        appendCopyLike(sb, "ADD", plan.add());

        // 7) COPY (JSON array 형식)
        appendCopyLike(sb, "COPY", plan.copy());

        // 8) RUN (한 줄 하나씩)
        appendRun(sb, plan.run());

        // 9) USER
        if (notBlank(plan.user())) {
            sb.append("USER ").append(plan.user()).append('\n');
        }

        // 10) VOLUME (JSON array)
        appendVolume(sb, plan.volume());

        // 11) EXPOSE
        appendExpose(sb, plan.expose());

        // 12) HEALTHCHECK
        appendHealthcheck(sb, plan.healthcheck());

        // 13) ENTRYPOINT (JSON array)
        appendJsonArrayInstr(sb, "ENTRYPOINT", plan.entrypoint());

        // 14) CMD (JSON array)
        appendJsonArrayInstr(sb, "CMD", plan.cmd());

        // 마지막 개행/공백 정리
        String dockerfile = trimTrailingNewlines(sb.toString());
        byte[] bytes = dockerfile.getBytes(StandardCharsets.UTF_8);
        GeneratedFile file = new GeneratedFile(DOCKERFILE, bytes, MediaType.TEXT_PLAIN, false, FileType.DOCKERFILE);

        return List.of(file);
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
            sb.append("# 환경 변수는 런타임시에 주입해주세요\n");
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
        if (notBlank(h.interval())) opts.add("--interval=" + h.interval());
        if (notBlank(h.timeout())) opts.add("--timeout=" + h.timeout());
        if (h.retries() != null) opts.add("--retries=" + h.retries());
        if (notBlank(h.startPeriod())) opts.add("--start-period=" + h.startPeriod());

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

    /* -------------------- utils -------------------- */

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
        return parts.stream().map(DockerfileRenderer::jsonQuote)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String jsonQuote(String s) {
        if (s == null) return "\"\"";
        // 최소한의 JSON escape
        String esc = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + esc + "\"";
    }

}
