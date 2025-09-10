package com.dockerinit.features.application.dockerfile.renderer.impl;

import com.dockerinit.features.application.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.application.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.application.dockerfile.renderer.DockerfileArtifactRenderer;
import com.dockerinit.features.renderer.AbstractManifestRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("dockerfile")
public class DockerManifestRenderer extends AbstractManifestRenderer<DockerfileRequest, DockerfilePlan> implements DockerfileArtifactRenderer {

}
