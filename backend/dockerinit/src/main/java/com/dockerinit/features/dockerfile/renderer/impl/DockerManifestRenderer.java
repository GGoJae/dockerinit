package com.dockerinit.features.dockerfile.renderer.impl;

import com.dockerinit.features.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.dockerfile.renderer.DockerfileArtifactRenderer;
import com.dockerinit.features.renderer.AbstractManifestRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("dockerfile")
public class DockerManifestRenderer extends AbstractManifestRenderer<DockerfileRequest, DockerfilePlan> implements DockerfileArtifactRenderer {

}
