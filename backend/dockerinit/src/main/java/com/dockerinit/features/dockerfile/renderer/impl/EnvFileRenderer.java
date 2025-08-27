package com.dockerinit.features.dockerfile.renderer.impl;

import com.dockerinit.features.dockerfile.model.ContentType;
import com.dockerinit.features.dockerfile.model.FileType;
import com.dockerinit.features.dockerfile.model.GeneratedFile;
import com.dockerinit.features.dockerfile.model.RenderContext;
import com.dockerinit.features.dockerfile.renderer.ArtifactRenderer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Order(10)
public class EnvFileRenderer implements ArtifactRenderer {

    private static final String DOT_ENV_DOT_EXAMPLE = ".env.example";


    @Override
    public FileType fileType() {
        return FileType.ENV;
    }

    @Override
    public boolean supports(RenderContext ctx) {
        return ctx.targets().contains(FileType.ENV);
    }

    @Override
    public List<GeneratedFile> render(RenderContext ctx, List<String> warnings) {
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
                ContentType.TEXT, true, FileType.ENV);

        return List.of(file);
    }
}
