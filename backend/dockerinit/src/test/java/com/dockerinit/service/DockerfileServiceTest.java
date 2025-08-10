package com.dockerinit.service;

import com.dockerinit.dockerfile.dto.DockerfilePreset;
import com.dockerinit.dockerfile.dto.DockerfileRequest;
import com.dockerinit.dockerfile.service.DockerfileService;
import com.dockerinit.global.constants.ErrorMessage;
import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.global.validation.DockerImageValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.dockerinit.global.constants.ErrorMessage.INVALID_DOCKER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DockerfileServiceTest {

    @Mock
    private DockerImageValidationService dockerImageValidationService;
    private DockerfileService dockerfileService;

    @BeforeEach
    void setUp() {
        dockerfileService = new DockerfileService(dockerImageValidationService);
    }

    @Test
    void generateDockerfile_정상_작동() {
        // given
        DockerfileRequest request = new DockerfileRequest();
        request.setBaseImage("openjdk:17");
        request.setWorkdir("/app");

        // when
        Mockito.when(dockerImageValidationService.existsInDockerHub("openjdk:17"))
                .thenReturn(true);

        String result = dockerfileService.generateDockerfile(request);

        //then
        assertThat(result).contains("FROM openjdk:17");
        assertThat(result).contains("WORKDIR /app");
    }

    @Test
    void generateDockerfile_유효하지않은_BaseImage_예외발생() {
        // given
        DockerfileRequest request = new DockerfileRequest();
        request.setBaseImage("random:random");
        request.setWorkdir("/app");

        // when
        Mockito.when(dockerImageValidationService.existsInDockerHub("random:random"))
                .thenReturn(false);

        // then
        assertThatThrownBy(() -> dockerfileService.generateDockerfile(request))
                .isInstanceOf(InvalidInputCustomException.class)
                .hasMessageContaining(INVALID_DOCKER_IMAGE);
    }

    @Test
    void getAllPresets_프리셋_3개_반환() {
        // when
        List<DockerfilePreset> presets = dockerfileService.getAllPresets();

        // then
        assertThat(presets)
                .hasSize(3)
                .extracting("name")
                .contains("Spring Boot JAR", "Node.js Express", "Python Flask");
    }

    @Test
    void getPresentByName_이름으로_검색() {
        // when
        DockerfilePreset preset1 = dockerfileService.getPresentByName("spring-boot-jar");
        DockerfilePreset preset2 = dockerfileService.getPresentByName("node-js-express");
        DockerfilePreset preset3 = dockerfileService.getPresentByName("python-flask");

        // then
        assertThat(preset1).extracting("name").isEqualTo("Spring Boot JAR");
        assertThat(preset2).extracting("name").isEqualTo("Node.js Express");
        assertThat(preset3).extracting("name").isEqualTo("Python Flask");
    }

    @Test
    void getPresentByName_없는_이름_검색() {
        // when & then
        assertThatThrownBy(() -> dockerfileService.getPresentByName("no-name"))
                .isInstanceOf(NotFoundCustomException.class)
                .hasMessageContaining(ErrorMessage.PRESET_NOT_FOUND);
    }

    @Test
    void downloadDockerfile() {
        // given
        DockerfileRequest request = new DockerfileRequest();
        request.setBaseImage("openjdk:17");
        request.setWorkdir("/app");

        Mockito.when(dockerImageValidationService.existsInDockerHub("openjdk:17"))
                .thenReturn(true);

        // when
        byte[] zipBytes = dockerfileService.downloadDockerfile(request);

        // then
        assertThat(zipBytes).isNotNull();
        assertThat(zipBytes.length).isGreaterThan(0);
    }
}