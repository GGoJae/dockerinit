package com.dockerinit.features.application.presetV2.compose.renderer;

import com.dockerinit.features.application.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.application.dockercompose.domain.composeCustom.Network;
import com.dockerinit.features.application.dockercompose.domain.composeCustom.Volume;
import com.dockerinit.features.application.dockercompose.domain.model.Service;
import com.dockerinit.features.application.presetV2.composeService.renderer.ServiceRendererV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ComposeRendererV2 {

    private final ServiceRendererV2 serviceRenderer;

    /** 전체 docker-compose.yml 렌더 */
    public String render(ComposePlan plan) {
        StringBuilder b = new StringBuilder();

        // Compose v2는 version 필드가 필수가 아니지만, 프론트 프리뷰와 맞추려면 아래 라인 유지
        b.append("version: \"3.9\"\n");
        b.append("services:\n");

        List<Service> services = safeList(plan.services());
        int idx = 1;
        for (Service s : services) {
            String name = serviceName(s);
            if (name == null || name.isBlank()) name = "service" + idx++;
            b.append("  ").append(name).append(":\n");

            // 서비스 본문 렌더링 후 들여쓰기 적용
            String body = serviceRenderer.render(s);
            for (String line : body.split("\n", -1)) {
                if (line.isEmpty()) continue;
                b.append("    ").append(line).append("\n");
            }
        }

        // networks
        if (!safeMap(plan.networks()).isEmpty()) {
            b.append("networks:\n");
            for (Map.Entry<String, Network> e : plan.networks().entrySet()) {
                b.append("  ").append(e.getKey()).append(":\n");
                if (e.getValue() != null && notBlank(e.getValue().driver())) {
                    b.append("    driver: ").append(e.getValue().driver()).append("\n");
                }
            }
        }

        // volumes
        if (!safeMap(plan.volumes()).isEmpty()) {
            b.append("volumes:\n");
            for (Map.Entry<String, Volume> e : plan.volumes().entrySet()) {
                b.append("  ").append(e.getKey()).append(":\n");
                if (e.getValue() != null && notBlank(e.getValue().driver())) {
                    b.append("    driver: ").append(e.getValue().driver()).append("\n");
                }
            }
        }

        return b.toString();
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private static String serviceName(Service s) {
        try {
            // record 스타일 name() 또는 일반 getName() 둘 다 시도
            try {
                var m = s.getClass().getMethod("name");
                Object v = m.invoke(s);
                if (v instanceof String str && !str.isBlank()) return str;
            } catch (NoSuchMethodException ignore) {}
            try {
                var m = s.getClass().getMethod("getName");
                Object v = m.invoke(s);
                if (v instanceof String str && !str.isBlank()) return str;
            } catch (NoSuchMethodException ignore) {}
        } catch (Exception ignore) {}
        return null;
    }
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private static <T> List<T> safeList(List<T> l) { return l == null ? List.of() : l; }
    private static <K,V> Map<K,V> safeMap(Map<K,V> m) { return m == null ? Map.of() : m; }
}
