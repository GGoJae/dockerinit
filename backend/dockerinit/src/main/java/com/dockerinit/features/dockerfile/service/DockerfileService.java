package com.dockerinit.features.dockerfile.service;

import com.dockerinit.features.dockerfile.util.DockerfileGenerator;
import com.dockerinit.global.constants.ErrorMessage;
import com.dockerinit.features.dockerfile.dto.DockerfilePreset;
import com.dockerinit.features.dockerfile.dto.DockerfileRequest;
import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.global.support.validation.DockerImageValidationService;
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

@Service
public class DockerfileService {

    private final DockerImageValidationService dockerImageValidationService;


    private final Map<String, DockerfilePreset> dockerfilePresets = new HashMap<>();

    public DockerfileService(DockerImageValidationService dockerImageValidationService) {
        this.dockerImageValidationService = dockerImageValidationService;
        dockerfilePresets.put("spring-boot-jar", springBootJar());
        dockerfilePresets.put("node-js-express", nodeJsExpress());
        dockerfilePresets.put("python-flask", pythonFlask());
    }

    public String generateDockerfile(DockerfileRequest request) {
        String baseImage = request.getBaseImage();
        if (!dockerImageValidationService.existsInDockerHub(baseImage)) {
            throw new InvalidInputCustomException(ErrorMessage.INVALID_DOCKER_IMAGE, Map.of("image", baseImage));
        };

        return DockerfileGenerator.generate(request);
    }

    public List<DockerfilePreset> getAllPresets() {
        return dockerfilePresets.values().stream().toList();
    }

    public DockerfilePreset getPresentByName(String name) {
        return Optional.ofNullable(dockerfilePresets.get(name))
                .orElseThrow(() -> new NotFoundCustomException(
                        ErrorMessage.PRESET_NOT_FOUND,
                        Map.of("presetName", name)
                ));
    }


    public byte[] downloadDockerfile(DockerfileRequest request) {
        String baseImage = request.getBaseImage();
        if (!dockerImageValidationService.existsInDockerHub(baseImage)) {
            throw new InvalidInputCustomException(ErrorMessage.INVALID_DOCKER_IMAGE, Map.of("image", baseImage));
        };
        String dockerfileContent = DockerfileGenerator.generate(request);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("Dockerfile");
            zos.putNextEntry(entry);
            zos.write(dockerfileContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }


    private DockerfilePreset springBootJar() {
        DockerfileRequest req = new DockerfileRequest();
        req.setBaseImage("openjdk:17");
        req.setWorkdir("/app");
        req.setCopy(List.of(new DockerfileRequest.CopyDirective(".", ".")));
        req.setEnvMode("prod");
        req.setEnvVars(Map.of("SPRING_PROFILES_ACTIVE", "prod"));
        req.setExpose(List.of(8080));
        req.setCmd(List.of("java", "-jar", "app.jar"));
        return new DockerfilePreset("Spring Boot JAR", DockerfileGenerator.generate(req));
    }

    private DockerfilePreset nodeJsExpress() {
        DockerfileRequest req = new DockerfileRequest();
        req.setBaseImage("node:20");
        req.setWorkdir("/app");
        req.setCopy(List.of(new DockerfileRequest.CopyDirective(".", ".")));
        req.setRun(List.of("npm install"));
        req.setEnvMode("prod");
        req.setEnvVars(Map.of("NODE_ENV", "production"));
        req.setExpose(List.of(3000));
        req.setCmd(List.of("node", "app.js"));
        return new DockerfilePreset("Node.js Express", DockerfileGenerator.generate(req));
    }

    private DockerfilePreset pythonFlask() {
        DockerfileRequest req = new DockerfileRequest();
        req.setBaseImage("python:3.11");
        req.setWorkdir("/app");
        req.setAdd(List.of(new DockerfileRequest.CopyDirective("requirements.txt", ".")));
        req.setCopy(List.of(new DockerfileRequest.CopyDirective(".", ".")));
        req.setRun(List.of("pip install -r requirements.txt"));
        req.setEnvMode("prod");
        req.setEnvVars(Map.of("FLASK_ENV", "production"));
        req.setExpose(List.of(5000));
        req.setCmd(List.of("python", "app.py"));
        return new DockerfilePreset("Python Flask", DockerfileGenerator.generate(req));
    }



}
