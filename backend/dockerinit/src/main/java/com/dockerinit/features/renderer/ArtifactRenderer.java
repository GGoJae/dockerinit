package com.dockerinit.features.renderer;

import com.dockerinit.features.model.RenderContext;
import com.dockerinit.features.model.GeneratedFile;

import java.util.List;

public interface ArtifactRenderer<RQ, PL, FT extends Enum<FT>> {
    FT fileType();

    List<GeneratedFile> render(RenderContext<RQ, PL, FT> ctx, List<String> warnings);

    boolean supports(RenderContext<RQ, PL, FT> ctx);

    default int order() {
        return 100;
    }

    default String id() {
        return fileType().name();
    }
}
