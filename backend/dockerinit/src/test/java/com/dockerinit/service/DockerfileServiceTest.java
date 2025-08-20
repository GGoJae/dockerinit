package com.dockerinit.service;

import com.dockerinit.features.dockerfile.packager.ArtifactPackager;
import com.dockerinit.features.dockerfile.render.DockerfileRenderer;
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
    private DockerfileRenderer dockerfileRenderer;
    @Mock
    private ArtifactPackager packager;
    private DockerfileService dockerfileService;

    @BeforeEach
    void setUp() {
        dockerfileService = new DockerfileService(dockerImageValidationService, dockerfileRenderer, packager);
    }
}