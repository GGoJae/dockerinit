package com.dockerinit.features.application.dockercompose.renderer.impl;

import com.dockerinit.features.application.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.application.dockercompose.domain.model.Service;
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
import java.util.*;
import java.util.stream.Collectors;

@Component
@Order(20)
@Qualifier("compose")
public class ComposeEnvRenderer implements ComposeArtifactRenderer {

    private static final String ENV_PATH = "compose/.env.example";

    @Override
    public FileType fileType() {
        return FileType.ENV;
    }

    @Override
    public boolean supports(RenderContext<ComposeRequestV1, ComposePlan> ctx) {
        return ctx.targets().contains(FileType.ENV);
    }

    @Override
    public List<GeneratedFile> render(RenderContext<ComposeRequestV1, ComposePlan> ctx, List<String> warnings) {
        List<Service> services = ctx.plan().services().stream()
                .sorted(Comparator.comparing(s -> s.name()))
                .toList();

        Map<String, String> merged = new LinkedHashMap<>();
        Set<String> conflicts = new TreeSet<>();

        for (Service s : services) {
            Map<String, String> env = s.environment();
            if (env == null || env.isEmpty()) continue;

            List<Map.Entry<String, String>> sortedEnv = env.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();

            for (Map.Entry<String, String> e : sortedEnv) {
                String k = e.getKey();
                String v = e.getValue();

                if (!merged.containsKey(k)) {
                    merged.put(k, v);
                } else {
                    String prev = merged.get(k);
                    if (!Objects.equals(prev, k)) {
                        merged.put(k, "");
                        conflicts.add(k);
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder(256);
        sb.append("# Example .env for Docker Compose").append('\n');
        sb.append("# project: ").append(ctx.plan().projectName()).append('\n');
        sb.append("# services: ")
                .append(services.stream().map(s -> s.name()).sorted().collect(Collectors.joining(", ")))
                .append('\n');

        if (!conflicts.isEmpty()) {
            sb.append("# NOTE: conflicting defaults detected for keys → ")
                    .append(String.join(", ", conflicts))
                    .append('\n');
        }
        sb.append('\n');

        if (merged.isEmpty()) {
            sb.append("# No environment variables declared in services.").append('\n');
            sb.append("# Add variables below and reference them as ${VAR} in compose.").append('\n');
        } else {
            merged.keySet().stream().sorted().forEach(k -> {
                String v = merged.get(k);
                if (v == null || v.isBlank()) {
                    sb.append(k).append('=').append('\n');
                } else {
                    sb.append(k).append('=').append(v).append('\n');
                }
            });
        }

        warnings.add(".env는 레포지토리에 커밋하지 말고 `docker compose --env-file .env up -d`로 사용하세요.");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        GeneratedFile file = new GeneratedFile(ENV_PATH, bytes, ContentType.TEXT, true, FileType.ENV);
        return List.of(file);
    }
}
