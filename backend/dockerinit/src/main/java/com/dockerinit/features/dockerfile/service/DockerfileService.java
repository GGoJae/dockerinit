package com.dockerinit.features.dockerfile.service;

import com.dockerinit.features.dockerfile.dto.request.DockerfilePresetRequest;
import com.dockerinit.features.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.dockerfile.dto.response.DockerfileResponse;
import com.dockerinit.features.dockerfile.mapper.DockerfilePlanMapper;
import com.dockerinit.features.dockerfile.model.RenderContext;
import com.dockerinit.features.dockerfile.model.PackageResult;
import com.dockerinit.features.dockerfile.packager.Packager;
import com.dockerinit.features.dockerfile.model.DockerfilePlan;
import com.dockerinit.features.dockerfile.renderer.ArtifactRenderer;
import com.dockerinit.features.dockerfile.model.FileType;
import com.dockerinit.features.dockerfile.model.GeneratedFile;
import com.dockerinit.features.support.validation.DockerImageValidationService;
import com.dockerinit.global.constants.ErrorMessage;
import com.dockerinit.global.exception.InternalErrorCustomException;
import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.exception.NotFoundCustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class DockerfileService {

    private final DockerImageValidationService dockerImageValidationService;
    private final List<ArtifactRenderer> renderers;
    private final Packager packager;

    public DockerfileService(DockerImageValidationService dockerImageValidationService, List<ArtifactRenderer> renderers, Packager packager) {
        this.dockerImageValidationService = dockerImageValidationService;
        this.renderers = renderers.stream().sorted(Comparator.comparingInt(ArtifactRenderer::order)).toList();
        this.packager = packager;
    }

    private final Map<String, DockerfilePresetRequest> dockerfilePresets = new HashMap<>();

    public PackageResult downloadPackageAsZip(DockerfileRequest request) {
        String baseImage = request.baseImage();
        if (!dockerImageValidationService.existsInDockerHub(baseImage)) {
            throw new InvalidInputCustomException(ErrorMessage.INVALID_DOCKER_IMAGE, Map.of("image", baseImage));
        };
        DockerfilePlan plan = DockerfilePlanMapper.toPlan(request);
        List<String> warnings = new ArrayList<>(plan.warnings());

        List<GeneratedFile> artifacts = new ArrayList<>();
        Set<FileType> targets = plan.targets();

        for (ArtifactRenderer r : renderers) {
            RenderContext ctx = new RenderContext(request, plan, targets, List.copyOf(artifacts));
            if (r.supports(ctx)) {
                try {
                    artifacts.addAll(r.render(ctx, warnings));
                } catch (Exception e) {
                    log.warn("랜더 도중 에러 {}", ctx, e);
                    warnings.add("Renderer '" + r.id() + "' failed ");
                }
            }
        }

        return packager.packageFiles(artifacts, "docker-artifacts");
    }

    public DockerfileResponse renderContent(DockerfileRequest request) {
        return null;    // TODO 도커파일만 문자열 타입으로 내보낼 로직 작성
    }

    public List<DockerfilePresetRequest> getAllPresets() {
        return dockerfilePresets.values().stream().toList();
    }

    public DockerfilePresetRequest getPreset(String name) {
        return Optional.ofNullable(dockerfilePresets.get(name))
                .orElseThrow(() -> new NotFoundCustomException(
                        ErrorMessage.PRESET_NOT_FOUND,
                        Map.of("presetName", name)
                ));
    }


//    private DockerfilePresetRequest springBootJar() {
//        DockerfileRequest req = new DockerfileRequest(
//                "openjdk:17",
//                "/app",
//                List.of(new CopyDirective(".", ".")),
//                null,
//                Mode.prod,
//                Map.of("SPRING_PROFILES_ACTIVE", "prod"),
//                List.of(8080),
//                List.of("java", "-jar", "app.jar"),
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null
//        );
//
//        DockerfileSpec spec = DockerfileSpecMapper.toSpec(req);
//        RenderResult render = renderer.render(spec);
//
//
//        return new DockerfilePresetRequest("Spring Boot JAR", render.dockerfile());
//    }
//        // TODO 프리셋 어떻게 할지 정하기. 리소스에 파일 만들어놓고 맵?
//    private DockerfilePresetRequest nodeJsExpress() {
//        DockerfileRequest req = new DockerfileRequest(
//                "node:20",
//                "/app",
//                List.of(new CopyDirective(".", ".")),
//                null,
//                Mode.prod,
//                Map.of("NODE_ENV", "production"),
//                List.of(3000),
//                List.of("node", "app.js"),
//                List.of("npm install"),
//                null, null, null, null, null, null
//        );
//
//        DockerfileSpec spec = DockerfileSpecMapper.toSpec(req);
//        RenderResult render = renderer.render(spec);
//        return new DockerfilePresetRequest("Node.js Express", render.dockerfile());
//    }
//
//    private DockerfilePresetRequest pythonFlask() {
//        DockerfileRequest req = new DockerfileRequest(
//                "python:3.11",
//                "/app",
//                List.of(new CopyDirective(".", ".")),
//                List.of(new CopyDirective("requirements.txt", ".")),
//                Mode.prod,
//                Map.of("FLASK_ENV", "production"),
//                List.of(5000),
//                List.of("python", "app.py"),
//                null, null, null, null, null, null, null
//        );
//
//        DockerfileSpec spec = DockerfileSpecMapper.toSpec(req);
//        RenderResult render = renderer.render(spec);
//
//        return new DockerfilePresetRequest("Python Flask", render.dockerfile());
//    }



}
