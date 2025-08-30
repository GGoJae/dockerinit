package com.dockerinit.features.dockercompose.randerer.impl;

import com.dockerinit.features.dockercompose.domain.ComposePlan;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.dockercompose.randerer.ComposeArtifactRenderer;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;

import java.util.List;

public class ComposeEnvRenderer implements ComposeArtifactRenderer {
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
        // TODO 환경변수 처리하는 로직
        return List.of();
    }
}
