package com.dockerinit.service;

import com.dockerinit.features.dockerfile.domain.DockerFileType;
import com.dockerinit.features.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.packager.Packager;
import com.dockerinit.features.renderer.ArtifactRenderer;
import com.dockerinit.features.dockerfile.service.DockerfileService;
import com.dockerinit.features.support.validation.DockerImageValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class DockerfileServiceTest {

    @Mock
    private DockerImageValidationService dockerImageValidationService;
    @Mock
    private List<ArtifactRenderer<DockerfileRequest, DockerfilePlan, DockerFileType>> artifactRenderers;
    @Mock
    private Packager packager;
    private DockerfileService dockerfileService;

    @BeforeEach
    void setUp() {
        dockerfileService = new DockerfileService(dockerImageValidationService, artifactRenderers, packager);
    }
}