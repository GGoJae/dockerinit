package com.dockerinit.features.model;

import java.util.List;
import java.util.Set;

public record RenderContext<RQ, PL, FT>(
        RQ req,
        PL plan,
        Set<FT> targets,
        List<GeneratedFile> untilNowArtifacts
) {}
