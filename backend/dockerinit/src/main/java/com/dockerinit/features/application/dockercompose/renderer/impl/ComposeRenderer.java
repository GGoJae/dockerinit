package com.dockerinit.features.application.dockercompose.renderer.impl;

import com.dockerinit.features.application.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.application.dockercompose.domain.Service;
import com.dockerinit.features.application.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.application.dockercompose.renderer.ComposeArtifactRenderer;
import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@Order(1)
@Qualifier("compose")
public class ComposeRenderer implements ComposeArtifactRenderer {
    @Override
    public FileType fileType() {
        return FileType.COMPOSE;
    }

    @Override
    public boolean supports(RenderContext<ComposeRequestV1, ComposePlan> ctx) {
        return ctx.targets().contains(FileType.COMPOSE);
    }

    @Override
    public List<GeneratedFile> render(RenderContext<ComposeRequestV1, ComposePlan> ctx, List<String> warnings) {
        ComposePlan p = ctx.plan();
        StringBuilder sb = new StringBuilder(1024);

        // version & services
        sb.append("version: \"3.9\"\n");
        sb.append("services:\n");
        for (Service s : p.services()) {
            indent(sb, 2).append(s.name()).append(":\n");
            if (s.image() != null && !s.image().isBlank()) {
                indent(sb, 4).append("image: ").append(quoteIfNeeded(s.image())).append('\n');
            } else if (s.build() != null) {
                indent(sb, 4).append("build:\n");
                indent(sb, 6).append("context: ").append(quoteIfNeeded(s.build().context())).append('\n');
                if (s.build().dockerfile() != null && !s.build().dockerfile().isBlank()) {
                    indent(sb, 6).append("dockerfile: ").append(quoteIfNeeded(s.build().dockerfile())).append('\n');
                }
                if (!s.build().args().isEmpty()) {
                    indent(sb, 6).append("args:\n");
                    for (Map.Entry<String, String> e : s.build().args().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                        indent(sb, 8).append(e.getKey()).append(": ").append(quoteIfNeeded(e.getValue())).append('\n');
                    }
                }
            }

            if (!s.command().isEmpty()) {
                indent(sb, 4).append("command: [");
                for (int i = 0; i < s.command().size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(quoteYaml(s.command().get(i)));
                }
                sb.append("]\n");
            }
            if (!s.environment().isEmpty()) {
                indent(sb, 4).append("environment:\n");
                s.environment().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e ->
                        indent(sb, 6).append(e.getKey()).append(": ").append(quoteIfNeeded(e.getValue())).append('\n')
                );
            }
            if (!s.envFile().isEmpty()) {
                indent(sb, 4).append("env_file:\n");
                s.envFile().forEach(f -> indent(sb, 6).append("- ").append(quoteIfNeeded(f)).append('\n'));
            }
            if (!s.ports().isEmpty()) {
                indent(sb, 4).append("ports:\n");
                s.ports().forEach(po -> indent(sb, 6).append("- ").append(quoteIfNeeded(po)).append('\n'));
            }
            if (!s.volumes().isEmpty()) {
                indent(sb, 4).append("volumes:\n");
                s.volumes().forEach(v -> indent(sb, 6).append("- ").append(quoteIfNeeded(v)).append('\n'));
            }
            if (!s.dependsOn().isEmpty()) {
                indent(sb, 4).append("depends_on:\n");
                s.dependsOn().forEach(d -> indent(sb, 6).append("- ").append(quoteIfNeeded(d)).append('\n'));
            }
            if (s.restart() != null && !s.restart().isBlank()) {
                indent(sb, 4).append("restart: ").append(quoteIfNeeded(s.restart())).append('\n');
            }
            if (s.healthcheck() != null &&  s.healthcheck().test() != null && !s.healthcheck().test().isBlank()) {
                indent(sb, 4).append("healthcheck:\n");
                indent(sb, 6).append("test: ").append(quoteIfNeeded(s.healthcheck().test())).append('\n');
                if (s.healthcheck().interval() != null)
                    indent(sb, 6).append("interval: ").append(s.healthcheck().interval()).append('\n');
                if (s.healthcheck().timeout() != null)
                    indent(sb, 6).append("timeout: ").append(s.healthcheck().timeout()).append('\n');
                if (s.healthcheck().retries() != null)
                    indent(sb, 6).append("retries: ").append(s.healthcheck().retries()).append('\n');
                if (s.healthcheck().startPeriod() != null)
                    indent(sb, 6).append("start_period: ").append(s.healthcheck().startPeriod()).append('\n');
            }
        }

        // networks
        if (!p.networks().isEmpty()) {
            sb.append("networks:\n");
            p.networks().forEach((name, net) -> {
                indent(sb, 2).append(name).append(":\n");
                if ( net.driver() != null && !net.driver().isBlank()) {
                    indent(sb, 4).append("driver: ").append(quoteIfNeeded(net.driver())).append('\n');
                }
            });
        }

        // volumes
        if (!p.volumes().isEmpty()) {
            sb.append("volumes:\n");
            p.volumes().forEach((name, vol) -> {
                indent(sb, 2).append(name).append(":\n");
                if ( vol.driver() != null && !vol.driver().isBlank()) {
                    indent(sb, 4).append("driver: ").append(quoteIfNeeded(vol.driver())).append('\n');
                }
            });
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        GeneratedFile out = new GeneratedFile("docker-compose.yml", bytes, ContentType.YAML, false, FileType.COMPOSE);
        return List.of(out);
    }

    /* helpers */
    private static StringBuilder indent(StringBuilder sb, int n) {
        return sb.append(" ".repeat(n));
    }

    private static String quoteIfNeeded(String s) {
        if (s == null) return "''";
        // 안전을 위해 단순 따옴표 감싸기(공백/콜론/해시 등)
        return "'" + s.replace("'", "''") + "'";
    }

    private static String quoteYaml(String s) {
        return quoteIfNeeded(s);
    }

}
