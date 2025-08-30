package com.dockerinit.features.dockercompose.randerer;

import com.dockerinit.features.dockercompose.domain.ComposePlan;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.renderer.ArtifactRenderer;

public interface ComposeArtifactRenderer extends ArtifactRenderer<ComposeRequestV1, ComposePlan> {
}
