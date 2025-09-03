package com.dockerinit.features.dockercompose.renderer.impl;

import com.dockerinit.features.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.dockercompose.renderer.ComposeArtifactRenderer;
import com.dockerinit.features.renderer.AbstractManifestRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("compose")
public class ComposeManifestRenderer extends AbstractManifestRenderer<ComposeRequestV1, ComposePlan> implements ComposeArtifactRenderer {
}
