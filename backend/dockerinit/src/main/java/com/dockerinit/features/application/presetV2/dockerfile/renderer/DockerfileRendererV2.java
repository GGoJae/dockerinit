package com.dockerinit.features.application.presetV2.dockerfile.renderer;

import com.dockerinit.features.application.dockerfile.domain.CopyEntry;
import com.dockerinit.features.application.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.application.dockerfile.domain.Healthcheck;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DockerfileRendererV2 {

    public String render(DockerfilePlan p) {
        List<String> out = new ArrayList<>();

        // FROM / WORKDIR
        lineIf(out, "FROM %s", p.baseImage());
        lineIf(out, "WORKDIR %s", nullIfBlank(p.workdir()));

        // COPY / ADD
        for (CopyEntry c : safeList(p.copy())) {
            if (isBlank(c.sourceRelPath()) || isBlank(c.targetAbsPath())) continue;
            out.add("COPY " + c.sourceRelPath() + " " + c.targetAbsPath());
        }
        for (CopyEntry c : safeList(p.add())) {
            if (isBlank(c.sourceRelPath()) || isBlank(c.targetAbsPath())) continue;
            out.add("ADD " + c.sourceRelPath() + " " + c.targetAbsPath());
        }

        // ARG
        for (Map.Entry<String,String> e : safeMap(p.args()).entrySet()) {
            if (isBlank(e.getKey())) continue;
            out.add("ARG " + e.getKey() + "=" + (e.getValue() == null ? "" : e.getValue()));
        }

        // ENV
        for (Map.Entry<String,String> e : safeMap(p.envVars()).entrySet()) {
            if (isBlank(e.getKey())) continue;
            out.add("ENV " + e.getKey() + "=" + quoteIfNeeded(e.getValue()));
        }

        // LABEL
        for (Map.Entry<String,String> e : safeMap(p.label()).entrySet()) {
            if (isBlank(e.getKey())) continue;
            out.add("LABEL " + e.getKey() + "=" + jsonString(e.getValue()));
        }

        // EXPOSE
        if (!safeList(p.expose()).isEmpty()) {
            out.add("EXPOSE " + join(" ", p.expose()));
        }

        // RUN
        for (String r : safeList(p.run())) {
            if (isBlank(r)) continue;
            out.add("RUN " + r);
        }

        // USER
        lineIf(out, "USER %s", nullIfBlank(p.user()));

        // HEALTHCHECK
        Healthcheck hc = p.healthcheck();
        if (hc != null && !isBlank(hc.cmd())) {
            List<String> opts = new ArrayList<>();
            if (!isBlank(hc.interval())) opts.add("--interval=" + hc.interval());
            if (!isBlank(hc.timeout())) opts.add("--timeout=" + hc.timeout());
            if (hc.retries() != null)   opts.add("--retries=" + hc.retries());
            if (!isBlank(hc.startPeriod())) opts.add("--start-period=" + hc.startPeriod());

            String cmd = hc.cmd().trim();
            // 입력이 ["CMD", ...] 형태면 그대로, 아니면 shell form으로 그대로 사용
            out.add(("HEALTHCHECK " + String.join(" ", opts) + " CMD " + cmd).trim());
        }

        // ENTRYPOINT / CMD : List<String> → JSON 배열
        if (!safeList(p.entrypoint()).isEmpty()) {
            out.add("ENTRYPOINT " + jsonArray(p.entrypoint()));
        }
        if (!safeList(p.cmd()).isEmpty()) {
            out.add("CMD " + jsonArray(p.cmd()));
        }

        // VOLUME : List<String> → JSON 배열
        if (!safeList(p.volume()).isEmpty()) {
            out.add("VOLUME " + jsonArray(p.volume()));
        }

        out.add(""); // trailing newline
        return String.join("\n", out);
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private static String jsonString(String s) {
        if (s == null) s = "";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
    private static String jsonArray(List<String> arr) {
        StringBuilder b = new StringBuilder("[");
        boolean first = true;
        for (String s : safeList(arr)) {
            if (!first) b.append(",");
            b.append(jsonString(s == null ? "" : s));
            first = false;
        }
        b.append("]");
        return b.toString();
    }
    private static String quoteIfNeeded(String v) {
        if (v == null) return "";
        if (v.contains(" ") || v.contains("\t") || v.contains("\"")) {
            return jsonString(v);
        }
        return v;
    }
    private static <T> List<T> safeList(List<T> l) { return l == null ? List.of() : l; }
    private static <K,V> Map<K,V> safeMap(Map<K,V> m) { return m == null ? Map.of() : m; }
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String nullIfBlank(String s) { return isBlank(s) ? null : s; }
    private static void lineIf(List<String> out, String fmt, String v) {
        if (v != null) out.add(String.format(fmt, v));
    }
    private static String join(String sep, List<?> vals) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (Object v : safeList(vals)) {
            if (!first) b.append(sep);
            b.append(String.valueOf(v));
            first = false;
        }
        return b.toString();
    }
}
