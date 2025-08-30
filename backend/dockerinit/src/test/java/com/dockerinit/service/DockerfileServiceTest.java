package com.dockerinit.service;

import com.dockerinit.features.dockerfile.renderer.DockerfileArtifactRenderer;
import com.dockerinit.features.packager.Packager;
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
    private List<DockerfileArtifactRenderer> renderers;
    @Mock
    private Packager packager;
    private DockerfileService dockerfileService;

    @BeforeEach
    void setUp() {
        dockerfileService = new DockerfileService(dockerImageValidationService, renderers, packager);
    }
}