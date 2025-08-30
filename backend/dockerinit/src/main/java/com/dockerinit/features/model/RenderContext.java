package com.dockerinit.features.model;

import java.util.List;
import java.util.Set;

public record RenderContext<RQ, PL>(
        RQ req,
        PL plan,
        Set<FileType> targets,
        List<GeneratedFile> untilNowArtifacts
) {}
