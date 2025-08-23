package com.dockerinit.service;

import com.dockerinit.features.dockerfile.packager.Packager;
import com.dockerinit.features.dockerfile.renderer.ArtifactRenderer;
import com.dockerinit.features.dockerfile.service.DockerfileService;
import com.dockerinit.features.support.validation.DockerImageValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DockerfileServiceTest {

    @Mock
    private DockerImageValidationService dockerImageValidationService;
    @Mock
    private ArtifactRenderer dockerfileRenderer;
    @Mock
    private Packager packager;
    private DockerfileService dockerfileService;

    @BeforeEach
    void setUp() {
        dockerfileService = new DockerfileService(dockerImageValidationService, dockerfileRenderer, packager);
    }
}