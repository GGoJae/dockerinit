package com.dockerinit.features.dockercompose.renderer;

import com.dockerinit.features.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.renderer.ArtifactRenderer;

public interface ComposeArtifactRenderer extends ArtifactRenderer<ComposeRequestV1, ComposePlan> {
}
