package com.dockerinit.features.dockerfile.renderer;

import com.dockerinit.features.dockerfile.domain.*;
import com.dockerinit.features.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.model.RenderContext;
import com.dockerinit.features.renderer.ArtifactRenderer;
import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.dockerfile.domain.DockerFileType;
import com.dockerinit.features.model.GeneratedFile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
@Order(30)
public class ReadmeRenderer implements ArtifactRenderer<DockerfileRequest, DockerfilePlan, DockerFileType> {

    private static final String README = "README.md";
    @Override
    public DockerFileType fileType() {
        return DockerFileType.README;
    }

    @Override
    public boolean supports(RenderContext<DockerfileRequest, DockerfilePlan, DockerFileType> ctx) {
        return ctx.targets().contains(DockerFileType.README);
    }

    @Override
    public List<GeneratedFile> render(RenderContext<DockerfileRequest, DockerfilePlan, DockerFileType> ctx, List<String> warnings) {
        DockerfilePlan plan = ctx.plan();
        Optional<GeneratedFile> dockerfile = ctx.untilNowArtifacts().stream()
                .filter(f -> f.fileType() == DockerFileType.DOCKERFILE)
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
                ContentType.MD, false, DockerFileType.README);
        return List.of(file);
    }
}
