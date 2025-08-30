package com.dockerinit.features.dockercompose.randerer.impl;

import com.dockerinit.features.dockercompose.domain.ComposePlan;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.dockercompose.randerer.ComposeArtifactRenderer;
import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

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

    // TODO ctx 활용해서 제대로 만들기
    @Override
    public List<GeneratedFile> render(RenderContext<ComposeRequestV1, ComposePlan> ctx, List<String> warnings) {
        String md = """
            # How to start with Docker Compose
            ```bash
            docker compose --env-file .env up -d
            docker compose logs -f
            docker compose down
            ```
            """;
        return List.of(new GeneratedFile(README, md.getBytes(StandardCharsets.UTF_8),
                ContentType.MD, false, FileType.README));
    }
}
