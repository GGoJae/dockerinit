package com.dockerinit.features.dockerfile.renderer;

import com.dockerinit.features.dockerfile.model.RenderContext;
import com.dockerinit.features.dockerfile.model.FileType;
import com.dockerinit.features.dockerfile.model.GeneratedFile;

import java.util.List;

public interface ArtifactRenderer {
    FileType fileType();

    List<GeneratedFile> render(RenderContext ctx, List<String> warnings);

    boolean supports(RenderContext ctx);

    default int order() {
        return 100;
    }

    default String id() {
        return fileType().name();
    }
}
