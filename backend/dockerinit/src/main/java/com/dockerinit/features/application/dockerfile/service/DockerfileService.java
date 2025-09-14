package com.dockerinit.features.application.dockerfile.service;

import com.dockerinit.features.application.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.application.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.application.dockerfile.dto.response.DockerfileResponse;
import com.dockerinit.features.application.dockerfile.mapper.DockerfilePlanMapper;
import com.dockerinit.features.application.dockerfile.renderer.DockerfileArtifactRenderer;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.PackageResult;
import com.dockerinit.features.model.RenderContext;
import com.dockerinit.features.packager.Packager;
import com.dockerinit.global.validation.DockerImageValidationService;
import com.dockerinit.global.constants.ErrorMessage;
import com.dockerinit.global.exception.InternalErrorCustomException;
import com.dockerinit.global.validation.ValidationCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class DockerfileService {

    private final DockerImageValidationService dockerImageValidationService;
    private final List<DockerfileArtifactRenderer> renderers;
    private final Packager packager;

    public DockerfileService(DockerImageValidationService dockerImageValidationService, List<DockerfileArtifactRenderer> renderers, Packager packager) {
        this.dockerImageValidationService = dockerImageValidationService;
        this.renderers = renderers.stream().sorted(Comparator.comparingInt(DockerfileArtifactRenderer::order)).toList();
        this.packager = packager;
    }

    public PackageResult downloadPackageAsZip(DockerfileRequest request) {
        String baseImage = request.baseImage();

        ValidationCollector.create()
                .requiredTrue(dockerImageValidationService.existsInDockerHub(baseImage),
                        "image", ErrorMessage.INVALID_DOCKER_IMAGE, baseImage)
                .throwIfInvalid();

        DockerfilePlan plan = DockerfilePlanMapper.toPlan(request);
        List<String> warnings = new ArrayList<>(plan.warnings());

        List<GeneratedFile> artifacts = new ArrayList<>();
        Set<FileType> targets = plan.targets();

        for (DockerfileArtifactRenderer r : renderers) {
            RenderContext<DockerfileRequest, DockerfilePlan> ctx = new RenderContext<>(request, plan, targets, List.copyOf(artifacts));
            if (r.supports(ctx)) {
                try {
                    artifacts.addAll(r.render(ctx, warnings));
                } catch (Exception e) {
                    log.warn("랜더러 '{}' failed. ctx= {}",r.id(),  ctx, e);
                    warnings.add("Renderer '" + r.id() + "' failed ");
                }
            }
        }

        return packager.packageFiles(artifacts, "docker-artifacts");
    }


    public DockerfileResponse renderContent(DockerfileRequest request) {

        String baseImage = request.baseImage();

        ValidationCollector.create()
                .requiredTrue(dockerImageValidationService.existsInDockerHub(baseImage), "image", ErrorMessage.INVALID_DOCKER_IMAGE,
                        baseImage)
                .throwIfInvalid();

        DockerfilePlan plan = DockerfilePlanMapper.toPlan(request);
        List<String> warnings = new ArrayList<>(plan.warnings());

        GeneratedFile dockerfile = renderSingleFile(request, plan, FileType.DOCKERFILE, warnings)
                .orElseThrow(() -> new InternalErrorCustomException("도커 파일 렌더 실패"));

        String content = new String(dockerfile.content(), StandardCharsets.UTF_8);

        return new DockerfileResponse(content, List.copyOf(warnings));
    }


    private Optional<GeneratedFile> renderSingleFile(DockerfileRequest request, DockerfilePlan plan, FileType type , List<String> warnings) {
        Set<FileType> targets = EnumSet.of(type);
        RenderContext<DockerfileRequest, DockerfilePlan> ctx = new RenderContext<>(request, plan, targets, List.of());

        for (DockerfileArtifactRenderer r : renderers) {
            if (r.fileType() != type) continue;
            if (!r.supports(ctx)) continue;
            try {
                List<GeneratedFile> out = r.render(ctx, warnings);
                return out.stream().findFirst();
            } catch (Exception e) {
                log.warn("렌더 에러: {}", r.id(), e);
                warnings.add("Renderer '" + r.id() + "' failed");
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

}
