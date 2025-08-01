package com.dockerinit.util;

import com.dockerinit.dto.dockerfile.DockerfileRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DockerfileGenerator {

    public static String generate(DockerfileRequest req) {
        StringBuilder sb = new StringBuilder();

        // 1. FROM
        sb.append("FROM ").append(req.getBaseImage()).append("\n");

        // 2. LABEL
        appendLabel(sb, req.getLabel());

        // 3. ARG
        appendArgs(sb, req.getArgs());

        // 4. ENV
        appendEnv(sb, req.getEnvVars(), req.getEnvMode());

        // 5. WORKDIR
        if (notEmpty(req.getWorkdir())) {
            sb.append("WORKDIR ").append(req.getWorkdir()).append("\n");
        }

        // 6. ADD
        appendCopyDirectives(sb, "ADD", req.getAdd());

        // 7. COPY
        appendCopyDirectives(sb, "COPY", req.getCopy());

        // 8. RUN
        appendListLines(sb, "RUN", req.getRun());

        // 9. USER
        if (notEmpty(req.getUser())) {
            sb.append("USER ").append(req.getUser()).append("\n");
        }

        // 10. VOLUME
        if (req.getVolume() != null && !req.getVolume().isEmpty()) {
            sb.append("VOLUME [").append(quoteJoin(req.getVolume())).append("]\n");
        }

        // 11. EXPOSE
        if (req.getExpose() != null) {
            req.getExpose().forEach(port -> sb.append("EXPOSE ").append(port).append("\n"));
        }

        // 12. HEALTHCHECK
        if (notEmpty(req.getHealthcheck())) {
            sb.append("HEALTHCHECK CMD ").append(req.getHealthcheck()).append("\n");
        }

        // 13. ENTRYPOINT
        if (req.getEntrypoint() != null && !req.getEntrypoint().isEmpty()) {
            sb.append("ENTRYPOINT ").append(jsonArray(req.getEntrypoint())).append("\n");
        }

        // 14. CMD
        if (req.getCmd() != null && !req.getCmd().isEmpty()) {
            sb.append("CMD ").append(jsonArray(req.getCmd())).append("\n");
        }

        return sb.toString().trim();
    }

    // ------- 헬퍼 메서드 --------
    private static void appendLabel(StringBuilder sb, Map<String, String> labels) {
        if (labels != null) {
            labels.forEach((k, v) -> sb.append("LABEL ").append(k).append("=\"").append(v).append("\"\n"));
        }
    }

    private static void appendArgs(StringBuilder sb, Map<String, String> args) {
        if (args != null) {
            args.forEach((k, v) -> sb.append("ARG ").append(k).append(v != null ? "=" + v : "").append("\n"));
        }
    }

    private static void appendEnv(StringBuilder sb, Map<String, String> envVars, String mode) {
        if (envVars != null && !envVars.isEmpty()) {
            String envMode = (mode != null) ? mode.toLowerCase() : "prod";

            if ("test".equals(envMode)) {
                envVars.forEach((k, v) -> sb.append("ENV ").append(k).append("=").append(v).append("\n"));
            } else {
                sb.append("# ENV 설정은 외부에서 주입하세요:\n");
                envVars.forEach((k, v) -> {
                    sb.append("# export ").append(k).append("=").append(v).append("\n");
                    sb.append("ENV ").append(k).append("=${").append(k).append("}\n");
                });
            }
        }
    }

    private static void appendCopyDirectives(StringBuilder sb, String directive, List<DockerfileRequest.CopyDirective> list) {
        if (list != null) {
            for (DockerfileRequest.CopyDirective d : list) {
                sb.append(directive).append(" ").append(d.source()).append(" ").append(d.target()).append("\n");
            }
        }
    }

    private static void appendListLines(StringBuilder sb, String prefix, List<String> lines) {
        if (lines != null) {
            lines.forEach(line -> sb.append(prefix).append(" ").append(line).append("\n"));
        }
    }

    private static String quoteJoin(List<String> list) {
        return String.join(", ", list.stream().map(s -> "\"" + s + "\"").toList());
    }

    private static String jsonArray(List<String> parts) {
        return "[" + quoteJoin(parts) + "]";
    }

    private static boolean notEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
