package com.dockerinit.features.application.dockerfile.renderer;

import com.dockerinit.features.application.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.application.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.renderer.ArtifactRenderer;


public interface DockerfileArtifactRenderer extends ArtifactRenderer<DockerfileRequest, DockerfilePlan> {
}
