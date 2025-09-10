package com.dockerinit.features.application.dockercompose.renderer;

import com.dockerinit.features.application.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.application.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.renderer.ArtifactRenderer;

public interface ComposeArtifactRenderer extends ArtifactRenderer<ComposeRequestV1, ComposePlan> {
}
