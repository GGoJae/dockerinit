package com.dockerinit.features.application.presetV2.composeService.renderer;

import com.dockerinit.features.application.dockercompose.domain.model.Build;
import com.dockerinit.features.application.dockercompose.domain.model.Service;
import com.dockerinit.features.application.dockercompose.domain.composeCustom.Healthcheck;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ServiceRendererV2 {

    /** 서비스 본문만 YAML로 렌더(상위에 서비스명 key는 포함하지 않음) */
    public String render(Service s) {
        StringBuilder b = new StringBuilder();
        final int I = 2; // indent size

        // image/build
        Build build = s.build();
        if (build != null) {
            append(b, 0, "build:");
            append(b, I, "context: %s", build.context());
            if (notBlank(build.dockerfile())) append(b, I, "dockerfile: %s", build.dockerfile());
            if (!safeMap(build.args()).isEmpty()) {
                append(b, I, "args:");
                for (Map.Entry<String, String> e : build.args().entrySet()) {
                    append(b, I * 2, "%s: %s", e.getKey(), quote(e.getValue()));
                }
            }
        } else if (notBlank(s.image())) {
            append(b, 0, "image: %s", s.image());
        }

        // command (List<String> → JSON array literal)
        if (!safeList(s.command()).isEmpty()) {
            append(b, 0, "command: %s", jsonArray(s.command()));
        }

        // environment (map)
        if (!safeMap(s.environment()).isEmpty()) {
            append(b, 0, "environment:");
            for (Map.Entry<String, String> e : s.environment().entrySet()) {
                append(b, I, "%s: %s", e.getKey(), quote(e.getValue()));
            }
        }

        // env_file (list)
        if (!safeList(s.envFile()).isEmpty()) {
            append(b, 0, "env_file:");
            for (String f : s.envFile()) append(b, I, "- %s", quote(f));
        }

        // ports (list of strings — 보통 따옴표로 감싸 안전하게 표현)
        if (!safeList(s.ports()).isEmpty()) {
            append(b, 0, "ports:");
            for (String p : s.ports()) append(b, I, "- %s", quote(p));
        }

        // volumes (list of strings, 일반적으로 그대로 출력)
        if (!safeList(s.volumes()).isEmpty()) {
            append(b, 0, "volumes:");
            for (String v : s.volumes()) append(b, I, "- %s", v);
        }

        // depends_on (list)
        if (!safeList(s.dependsOn()).isEmpty()) {
            append(b, 0, "depends_on:");
            for (String d : s.dependsOn()) append(b, I, "- %s", d);
        }

        // restart
        if (notBlank(s.restart())) {
            append(b, 0, "restart: %s", s.restart());
        }

        // healthcheck
        Healthcheck hc = s.healthcheck();
        if (hc != null && notBlank(hc.test())) {
            append(b, 0, "healthcheck:");
            String test = hc.test().trim();
            if (test.startsWith("[")) {
                // exec-form JSON array string 그대로
                append(b, I, "test: %s", test);
            } else {
                // shell-form
                append(b, I, "test: %s", quote(test));
            }
            if (notBlank(hc.interval()))     append(b, I, "interval: %s", hc.interval());
            if (notBlank(hc.timeout()))      append(b, I, "timeout: %s", hc.timeout());
            if (hc.retries() != null)        append(b, I, "retries: %d", hc.retries());
            if (notBlank(hc.startPeriod()))  append(b, I, "start_period: %s", hc.startPeriod());
        }

        return b.toString();
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private static void append(StringBuilder b, int indent, String fmt, Object... args) {
        b.append(" ".repeat(Math.max(0, indent)));
        b.append(String.format(fmt, args)).append("\n");
    }
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private static <T> List<T> safeList(List<T> l) { return l == null ? List.of() : l; }
    private static <K, V> Map<K, V> safeMap(Map<K, V> m) { return m == null ? Map.of() : m; }

    /** 값에 공백/특수문자가 있으면 따옴표로 감싸고 이스케이프 */
    private static String quote(String s) {
        if (s == null) return "\"\"";
        // 안전 문자만 포함되면 그대로
        if (s.matches("^[A-Za-z0-9._:@%+/\\-]+$")) return s;
        // 그 외는 double-quote
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /** YAML 내 JSON array literal 생성 */
    private static String jsonArray(List<String> arr) {
        StringBuilder b = new StringBuilder("[");
        boolean first = true;
        for (String s : safeList(arr)) {
            if (!first) b.append(", ");
            b.append(quote(s));
            first = false;
        }
        b.append("]");
        return b.toString();
    }
}

