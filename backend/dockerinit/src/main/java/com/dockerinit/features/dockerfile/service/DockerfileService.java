package com.dockerinit.features.dockerfile.service;

import com.dockerinit.features.dockerfile.dto.DockerfilePreset;
import com.dockerinit.features.dockerfile.dto.DockerfileRequest;
import com.dockerinit.features.dockerfile.dto.DockerfileResponse;
import com.dockerinit.features.dockerfile.mapper.DockerfileSpecMapper;
import com.dockerinit.features.dockerfile.model.DockerfileSpec;
import com.dockerinit.features.dockerfile.render.DockerfileRenderer;
import com.dockerinit.features.support.FileResult;
import com.dockerinit.features.support.validation.DockerImageValidationService;
import com.dockerinit.global.constants.ErrorMessage;
import com.dockerinit.global.exception.InternalErrorCustomException;
import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.exception.NotFoundCustomException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.dockerinit.global.constants.HttpInfo.APPLICATION_ZIP_VALUE;

@Service
public class DockerfileService {

    private final DockerImageValidationService dockerImageValidationService;
    private final DockerfileRenderer renderer;

    private final Map<String, DockerfilePreset> dockerfilePresets = new HashMap<>();

    public DockerfileService(DockerImageValidationService dockerImageValidationService, DockerfileRenderer renderer) {
        this.dockerImageValidationService = dockerImageValidationService;
        this.renderer = renderer;
        dockerfilePresets.put("spring-boot-jar", springBootJar());
        dockerfilePresets.put("node-js-express", nodeJsExpress());
        dockerfilePresets.put("python-flask", pythonFlask());
    }

    public DockerfileResponse renderContent(DockerfileRequest request) {
        String baseImage = request.baseImage();
        if (!dockerImageValidationService.existsInDockerHub(baseImage)) {
            throw new InvalidInputCustomException(ErrorMessage.INVALID_DOCKER_IMAGE, Map.of("image", baseImage));
        };

        DockerfileSpec spec = DockerfileSpecMapper.toSpec(request);

        String content = renderer.render(spec);

        return new DockerfileResponse(content);
    }

    public List<DockerfilePreset> getAllPresets() {
        return dockerfilePresets.values().stream().toList();
    }

    public DockerfilePreset getPreset(String name) {
        return Optional.ofNullable(dockerfilePresets.get(name))
                .orElseThrow(() -> new NotFoundCustomException(
                        ErrorMessage.PRESET_NOT_FOUND,
                        Map.of("presetName", name)
                ));
    }


    public FileResult buildZip(DockerfileRequest request) {
        String baseImage = request.baseImage();
        if (!dockerImageValidationService.existsInDockerHub(baseImage)) {
            throw new InvalidInputCustomException(ErrorMessage.INVALID_DOCKER_IMAGE, Map.of("image", baseImage));
        };

        DockerfileSpec spec = DockerfileSpecMapper.toSpec(request);

        String dockerfileContent = renderer.render(spec);

        byte[] zipBytes = buildDeterministicZip(dockerfileContent);

        ByteArrayResource resource = new ByteArrayResource(zipBytes);

        return new FileResult(
                resource,
                zipBytes.length,
                "dockerfile-template.zip",
                MediaType.parseMediaType(APPLICATION_ZIP_VALUE),
                null        // TODO eTag 넣는 로직 작성
        );
    }

    private byte[] buildDeterministicZip(String dockerfileContent) {
        byte[] zipBytes;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            zos.putNextEntry(new ZipEntry("Dockerfile"));
            zos.write(dockerfileContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.finish();
            zipBytes = baos.toByteArray();
        } catch (IOException e) {
            throw new InternalErrorCustomException("도커 파일 zip byte 민드는 도중 예외", e);
        }
        return zipBytes;
    }


    private DockerfilePreset springBootJar() {
        DockerfileRequest req = new DockerfileRequest(
                "openjdk:17",
                "/app",
                List.of(new DockerfileRequest.CopyDirective(".", ".")),
                null,
                DockerfileRequest.EnvModeDTO.prod,
                Map.of("SPRING_PROFILES_ACTIVE", "prod"),
                List.of(8080),
                List.of("java", "-jar", "app.jar"),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        DockerfileSpec spec = DockerfileSpecMapper.toSpec(req);
        String content = renderer.render(spec);


        return new DockerfilePreset("Spring Boot JAR", content);
    }
        // TODO 프리셋 어떻게 할지 정하기. 리소스에 파일 만들어놓고 맵?
    private DockerfilePreset nodeJsExpress() {
        DockerfileRequest req = new DockerfileRequest(
                "node:20",
                "/app",
                List.of(new DockerfileRequest.CopyDirective(".", ".")),
                null,
                DockerfileRequest.EnvModeDTO.prod,
                Map.of("NODE_ENV", "production"),
                List.of(3000),
                List.of("node", "app.js"),
                List.of("npm install"),
                null, null, null, null, null, null
        );

        DockerfileSpec spec = DockerfileSpecMapper.toSpec(req);
        String content = renderer.render(spec);
        return new DockerfilePreset("Node.js Express", content);
    }

    private DockerfilePreset pythonFlask() {
        DockerfileRequest req = new DockerfileRequest(
                "python:3.11",
                "/app",
                List.of(new DockerfileRequest.CopyDirective(".", ".")),
                List.of(new DockerfileRequest.CopyDirective("requirements.txt", ".")),
                DockerfileRequest.EnvModeDTO.prod,
                Map.of("FLASK_ENV", "production"),
                List.of(5000),
                List.of("python", "app.py"),
                null, null, null, null, null, null, null
        );

        DockerfileSpec spec = DockerfileSpecMapper.toSpec(req);
        String content = renderer.render(spec);

        return new DockerfilePreset("Python Flask", content);
    }



}
