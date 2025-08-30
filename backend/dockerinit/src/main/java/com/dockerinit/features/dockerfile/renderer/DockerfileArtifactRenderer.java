package com.dockerinit.features.dockerfile.renderer;

import com.dockerinit.features.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.renderer.ArtifactRenderer;


public interface DockerfileArtifactRenderer extends ArtifactRenderer<DockerfileRequest, DockerfilePlan> {
}
