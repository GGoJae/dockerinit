package com.dockerinit.features.dockerfile.renderer.impl;

import com.dockerinit.features.dockerfile.model.*;
import com.dockerinit.features.dockerfile.renderer.ArtifactRenderer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
@Order(30)
public class ReadmeRenderer implements ArtifactRenderer {

    private static final String README = "README.md";
    @Override
    public FileType fileType() {
        return FileType.README;
    }

    @Override
    public boolean supports(RenderContext ctx) {
        return ctx.targets().contains(FileType.README);
    }

    @Override
    public List<GeneratedFile> render(RenderContext ctx, List<String> warnings) {
        DockerfilePlan plan = ctx.plan();
        Optional<GeneratedFile> dockerfile = ctx.untilNowArtifacts().stream()
                .filter(f -> f.fileType() == FileType.DOCKERFILE)
                .findFirst();

        String md = """
        # Docker Artifacts

        - Base image: %s
        - Workdir: %s

        ## Build
        ```bash
        docker build -t myapp .
        ```

        ## Run
        ```bash
        docker run --rm myapp
        ```

        > Tip: `.env`는 Dockerfile로 COPY하지 말고 `--env-file .env` 또는 compose의 `env_file:`로 사용하세요.

        %s
        """.formatted(
                plan.baseImage(),
                plan.workdir() == null ? "-" : plan.workdir(),
                dockerfile.map(df -> "\n<details>\n<summary>Dockerfile</summary>\n\n```dockerfile\n"
                        + new String(df.content(), StandardCharsets.UTF_8) + "\n```\n</details>\n").orElseGet(() -> "")
        );


        GeneratedFile file = new GeneratedFile(README, md.getBytes(StandardCharsets.UTF_8),
                ContentType.MD, false, FileType.README);
        return List.of(file);
    }
}
