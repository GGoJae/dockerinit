package com.dockerinit.features.dockercompose.service;

import com.dockerinit.features.dockercompose.domain.ComposePlan;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.dockercompose.dto.response.ComposeAsStringResponse;
import com.dockerinit.features.dockercompose.dto.response.ComposePresetResponse;
import com.dockerinit.features.dockercompose.mapper.ComposePlanMapper;
import com.dockerinit.features.dockercompose.renderer.ComposeArtifactRenderer;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.PackageResult;
import com.dockerinit.features.model.RenderContext;
import com.dockerinit.features.packager.Packager;
import com.dockerinit.features.support.FileResult;
import com.dockerinit.features.support.Hash;
import com.dockerinit.features.support.validation.DockerImageValidationService;
import com.dockerinit.global.constants.ErrorMessage;
import com.dockerinit.global.exception.InternalErrorCustomException;
import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.exception.NotFoundCustomException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.dockerinit.global.constants.HttpInfo.APPLICATION_ZIP_VALUE;

@Slf4j
@Service
public class DockerComposeService {

    private final DockerImageValidationService dockerImageValidationService;
    private final List<ComposeArtifactRenderer> renderers;
    private final Packager packager;

    private final Map<String, ComposePresetResponse> presetMap = new HashMap<>();


    public DockerComposeService(DockerImageValidationService dockerImageValidationService, List<ComposeArtifactRenderer> renderers, Packager packager) {
        this.dockerImageValidationService = dockerImageValidationService;
        this.renderers = renderers;
        this.packager = packager;
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = this.getClass().getResourceAsStream("/data/docker-compose-presets.json");
            TypeReference<Map<String, ComposePresetResponse>> typeRef = new TypeReference<>() {};
            presetMap.putAll(mapper.readValue(is, typeRef));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ComposePresetResponse> getAllPresets() {
        return presetMap.values().stream().toList();
    }

    public ComposePresetResponse getPreset(String name) {
        return Optional.ofNullable(presetMap.get(name))
                .orElseThrow(() -> new NotFoundCustomException(
                        ErrorMessage.PRESET_NOT_FOUND,
                        Map.of("presetName", name)
                ));
    }

    public FileResult getPresetAsZip(String name) {
        ComposePresetResponse preset = Optional.ofNullable(presetMap.get(name))
                .orElseThrow(() -> new NotFoundCustomException(
                        ErrorMessage.PRESET_NOT_FOUND,
                        Map.of("presetName", name)
                ));

        byte[] zipBytes = buildDeterministicZip(
                "docker-compose.yml",
                preset.ymlContent().getBytes(StandardCharsets.UTF_8));

        String eTag = getETag(zipBytes);

        ByteArrayResource resource = new ByteArrayResource(zipBytes);
        return new FileResult(
                resource,
                zipBytes.length,
                name + ".zip",
                MediaType.parseMediaType(APPLICATION_ZIP_VALUE),
                eTag);
    }

    public PackageResult downloadComposePackage(ComposeRequestV1 request) {
        ComposePlan plan = ComposePlanMapper.toPlan(request);
        ArrayList<String> warnings = new ArrayList<>(plan.warnings());

        List<GeneratedFile> artifacts = new ArrayList<>();
        Set<FileType> targets = EnumSet.of(FileType.COMPOSE);

        RenderContext<ComposeRequestV1, ComposePlan> ctx = new RenderContext<>(request, plan, targets, List.copyOf(artifacts));
        renderers.stream()
                .filter(r -> r.supports(ctx))
                .sorted(Comparator.comparingInt(r -> r.order()))
                .forEach(r -> artifacts.addAll(r.render(ctx, warnings)));

        return packager.packageFiles(artifacts, plan.projectName());
    }

    public ComposeAsStringResponse renderComposeYaml(ComposeRequestV1 request) {
        ComposePlan plan = ComposePlanMapper.toPlan(request);
        List<String> warnings = new ArrayList<>(plan.warnings());

        RenderContext<ComposeRequestV1, ComposePlan> ctx = new RenderContext<>(request, plan, EnumSet.of(FileType.COMPOSE), List.of());

        GeneratedFile file = renderSingleFile(request, plan, FileType.COMPOSE, warnings)
                .orElseThrow(() -> new IllegalArgumentException("컴포즈 렌더 실패"));

        String content = new String(file.content(), StandardCharsets.UTF_8);

        return new ComposeAsStringResponse(content, warnings);
    }

    private Optional<GeneratedFile> renderSingleFile(ComposeRequestV1 request, ComposePlan plan, FileType type , List<String> warnings) {
        Set<FileType> targets = EnumSet.of(type);
        RenderContext<ComposeRequestV1, ComposePlan> ctx = new RenderContext<>(request, plan, targets, List.of());

        for (ComposeArtifactRenderer r : renderers) {
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


    private static String getETag(byte[] zipBytes) {
        String rawETag = Hash.sha256Hex(zipBytes);
        return Objects.nonNull(rawETag) ? "\"" + rawETag + "\"" : null;
    }

    private static byte[] buildDeterministicZip(String entryName, byte[] content) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry entry = new ZipEntry(entryName);
            FileTime fileTime = FileTime.from(Instant.EPOCH);
            entry.setCreationTime(fileTime);
            entry.setLastModifiedTime(fileTime);
            entry.setLastAccessTime(fileTime);
            zos.putNextEntry(entry);
            zos.write(content);
            zos.closeEntry();
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new InternalErrorCustomException("도커 컴포즈 zip byte 민드는 도중 예외", e);
        }
    }

    private void validateImages(ComposeRequestV1 request) {
        Map<String, String> target = request.services().stream().collect(Collectors.toMap(
                s -> s.name(),
                s -> s.image(),
                (a, b) -> a
        ));

        Map<String, Object> invalidImages = new HashMap<>();

        target.forEach((type, image) -> {
            if (image != null && !dockerImageValidationService.existsInDockerHub(image)) {
                invalidImages.put(type, image);
            }
        });

        if (hasError(invalidImages)) {
            throw new InvalidInputCustomException("유효하지 않은 Docker 이미지 입니다.", invalidImages);
        }
    }

    private boolean hasError(Map<String, Object> bindingResult) {
        return !bindingResult.isEmpty();
    }
}
