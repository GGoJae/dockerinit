package com.dockerinit.features.dockerfile.renderer;

import com.dockerinit.features.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.dockerfile.domain.DockerFileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;
import com.dockerinit.features.renderer.ArtifactRenderer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Order(10)
public class EnvFileRenderer implements ArtifactRenderer<DockerfileRequest, DockerfilePlan, DockerFileType> {

    private static final String DOT_ENV_DOT_EXAMPLE = ".env.example";


    @Override
    public DockerFileType fileType() {
        return DockerFileType.ENV;
    }

    @Override
    public boolean supports(RenderContext<DockerfileRequest, DockerfilePlan, DockerFileType> ctx) {
        return ctx.targets().contains(DockerFileType.ENV);
    }

    @Override
    public List<GeneratedFile> render(RenderContext<DockerfileRequest, DockerfilePlan, DockerFileType> ctx, List<String> warnings) {
        var env = ctx.plan().envVars();
        var builder = new StringBuilder();

        if (env.isEmpty()) {
            builder.append("# 여기에 환경 변수를 넣어주세요\n");
        } else {
            env.keySet().stream().sorted().forEach(k -> {
                // 기본은 마스킹 없이 그대로. 필요하면 마스킹 정책 Plan에 넣자.
                builder.append(k).append('=').append(env.get(k)).append('\n');
            });
        }

        warnings.add("주의: .env는 이미지에 포함하지 말고 `--env-file .env` 또는 compose의 `env_file:`로 사용하세요.");

        GeneratedFile file = new GeneratedFile(DOT_ENV_DOT_EXAMPLE, builder.toString().getBytes(StandardCharsets.UTF_8),
                ContentType.TEXT, true, DockerFileType.ENV);

        return List.of(file);
    }
}
