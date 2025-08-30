package com.dockerinit.features.dockercompose.renderer.impl;

import com.dockerinit.features.dockercompose.domain.ComposePlan;
import com.dockerinit.features.dockercompose.domain.Service;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.dockercompose.renderer.ComposeArtifactRenderer;
import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Order(90)
@Qualifier("compose")
public class ComposeReadmeRenderer implements ComposeArtifactRenderer {

    private static final String README = "README.md";

    @Override
    public FileType fileType() {
        return FileType.README;
    }

    @Override
    public boolean supports(RenderContext<ComposeRequestV1, ComposePlan> ctx) {
        return ctx.targets().contains(FileType.README);
    }

    @Override
    public List<GeneratedFile> render(RenderContext<ComposeRequestV1, ComposePlan> ctx, List<String> warnings) {
        ComposePlan plan = ctx.plan();
        List<Service> services = plan.services().stream()
                .sorted(Comparator.comparing(s -> s.name()))
                .toList();

        boolean hasBuild = services.stream().anyMatch(s -> Objects.nonNull(s.build()));
        boolean usesEnvFile = true;

        String svcList = services.isEmpty()
                ? "(none)"
                : services.stream().map(Service::name).collect(Collectors.joining(", "));

        String portsSummary = services.stream()
                .filter(s -> !s.ports().isEmpty())
                .map(s -> "- " + s.name() + ": " + String.join(", ", s.ports()))
                .collect(Collectors.joining("\n"));

        String volumesSummary = services.stream()
                .filter(s -> !s.volumes().isEmpty())
                .map(s -> "- " + s.name() + ": " + String.join(", ", s.volumes()))
                .collect(Collectors.joining("\n"));

        String md = """
            # Docker Compose - %s

            **Services**: %s

            ## Quick Start
            ```bash
            %s
            docker compose %s up -d
            docker compose ps
            docker compose logs -f
            ```
            
            ## Stop & Clean
            ```bash
            docker compose down
            ```

            %s
            %s
            """.formatted(
                plan.projectName(),
                svcList,
                hasBuild ? "docker compose build" : "# (no build step required)",
                usesEnvFile ? "--env-file .env" : "",
                portsSummary.isBlank() ? "" : "## Ports\n" + portsSummary + "\n",
                volumesSummary.isBlank() ? "" : "## Volumes\n" + volumesSummary + "\n"
        );


        GeneratedFile file = new GeneratedFile(README, md.getBytes(StandardCharsets.UTF_8),
                ContentType.MD, false, FileType.README);

        return List.of(file);
    }
}
