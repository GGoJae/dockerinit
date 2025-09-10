package com.dockerinit.features.application.dockerfile.renderer.impl;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.application.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.application.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.application.dockerfile.renderer.DockerfileArtifactRenderer;
import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@Order(10)
@Qualifier("dockerfile")
public class DockerEnvRenderer implements DockerfileArtifactRenderer {

    private static final String DOT_ENV_DOT_EXAMPLE = ".env.example";


    @Override
    public FileType fileType() {
        return FileType.ENV;
    }

    @Override
    public boolean supports(RenderContext<DockerfileRequest, DockerfilePlan> ctx) {
        return ctx.targets().contains(FileType.ENV);
    }

    @Override
    public List<GeneratedFile> render(RenderContext<DockerfileRequest, DockerfilePlan> ctx, List<String> warnings) {
        Map<String, String> env = ctx.plan().envVars();
        StringBuilder sb = new StringBuilder();

        if (env.isEmpty()) {
            sb.append("# 여기에 환경 변수를 넣어주세요\n");
        } else {
            env.keySet().stream().sorted().forEach(k -> {
                // 기본은 마스킹 없이 그대로. 필요하면 마스킹 정책 Plan에 넣자.
                sb.append(k).append('=').append(env.get(k)).append('\n');
            });
        }

        warnings.add("주의: .env는 이미지에 포함하지 말고 `--env-file .env` 또는 compose의 `env_file:`로 사용하세요.");

        GeneratedFile file = new GeneratedFile(DOT_ENV_DOT_EXAMPLE, sb.toString().getBytes(StandardCharsets.UTF_8),
                ContentType.TEXT, true, FileType.ENV);

        return List.of(file);
    }
}
