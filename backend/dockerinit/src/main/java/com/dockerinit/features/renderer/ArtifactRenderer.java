package com.dockerinit.features.renderer;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.RenderContext;
import com.dockerinit.features.model.GeneratedFile;

import java.util.List;

public interface ArtifactRenderer<RQ, PL> {
    FileType fileType();

    boolean supports(RenderContext<RQ, PL> ctx);

    List<GeneratedFile> render(RenderContext<RQ, PL> ctx, List<String> warnings);

    default int order() {
        return 100;
    }

    default String id() {
        return fileType().name();
    }
}
