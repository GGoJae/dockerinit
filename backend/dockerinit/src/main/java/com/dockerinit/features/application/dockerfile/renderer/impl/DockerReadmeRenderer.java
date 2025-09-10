package com.dockerinit.features.application.dockerfile.renderer.impl;

import com.dockerinit.features.application.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.application.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.application.dockerfile.renderer.DockerfileArtifactRenderer;
import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
@Order(30)
@Qualifier("dockerfile")
public class DockerReadmeRenderer implements DockerfileArtifactRenderer {

    private static final String README = "README.md";
    @Override
    public FileType fileType() {
        return FileType.README;
    }

    @Override
    public boolean supports(RenderContext<DockerfileRequest, DockerfilePlan> ctx) {
        return ctx.targets().contains(FileType.README);
    }

    @Override
    public List<GeneratedFile> render(RenderContext<DockerfileRequest, DockerfilePlan> ctx, List<String> warnings) {
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
