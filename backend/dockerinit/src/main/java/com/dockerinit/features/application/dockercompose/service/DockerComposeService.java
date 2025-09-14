package com.dockerinit.features.application.dockercompose.service;

import com.dockerinit.features.application.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.application.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.application.dockercompose.dto.response.ComposeAsStringResponse;
import com.dockerinit.features.application.dockercompose.dto.spec.SelectionKind;
import com.dockerinit.features.application.dockercompose.mapper.ComposePlanMapper;
import com.dockerinit.features.application.dockercompose.renderer.ComposeArtifactRenderer;
import com.dockerinit.features.application.dockercompose.repository.ComposeServicePresetRepository;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.PackageResult;
import com.dockerinit.features.model.RenderContext;
import com.dockerinit.features.packager.Packager;
import com.dockerinit.global.validation.DockerImageValidationService;
import com.dockerinit.global.validation.ValidationErrors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerComposeService {

    private final DockerImageValidationService dockerImageValidationService;
    private final List<ComposeArtifactRenderer> renderers;
    private final Packager packager;
    private final ComposeServicePresetRepository repository;

    private static final Pattern IMAGE = Pattern.compile(
            "^[a-z0-9]+([._-][a-z0-9]+)*/?[a-z0-9._-]+(:[a-zA-Z0-9._-]+)?(@sha256:[a-f0-9]{64})?$"
    );

    public PackageResult downloadComposePackage(ComposeRequestV1 request) {
        validateImages(request);
        ComposePlan plan = ComposePlanMapper.toPlan(request);
        ArrayList<String> warnings = new ArrayList<>(plan.warnings());

        List<GeneratedFile> artifacts = new ArrayList<>();
        Set<FileType> targets = EnumSet.of(FileType.COMPOSE);

        RenderContext<ComposeRequestV1, ComposePlan> ctx = new RenderContext<>(request, plan, targets, List.copyOf(artifacts));
        renderers.stream()
                .filter(r -> r.supports(ctx))
                .sorted(Comparator.comparingInt(r -> r.order()))
                .forEach(r -> artifacts.addAll(r.render(ctx, warnings)));

        request.services().stream()
                .filter(s -> (s.kind() == SelectionKind.PRESET || s.kind() == SelectionKind.PRESET_OVERRIDDEN))
                .forEach(si -> repository.increaseUsedCountBySlug(si.presetSlug(), 1L));

        return packager.packageFiles(artifacts, plan.projectName());
    }

    public ComposeAsStringResponse renderComposeYaml(ComposeRequestV1 request) {
        validateImages(request);
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

    private void validateImages(ComposeRequestV1 request) {

        Map<String, String> target = request.services().stream().collect(Collectors.toMap(
                s -> s.service().name(),
                s -> s.service().image(),
                (a, b) -> a
        ));

        ValidationErrors ve = ValidationErrors.create().topMessage("유효하지 않은 Docker 이미지 입니다.");

        ve.forEachValueRejectIf("services", target,
                v -> v != null && !v.isBlank() && !IMAGE.matcher(v).matches(),
                "이미지 이름 형식이 올바르지 않습니다.");

        Map<String, String> okSyntax = target.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isBlank() && IMAGE.matcher(e.getValue()).matches())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<String> unique = okSyntax.values().stream().distinct().toList();

        Map<String, Boolean> exists = new HashMap<>(unique.size());
        for (String img : unique) {
            exists.put(img, dockerImageValidationService.existsInDockerHub(img));
        }

        ve.forEachValueRejectIf("services", okSyntax,
                v -> Boolean.FALSE.equals(exists.get(v)),
                "Docker Hub 에 존재하지 않는 이미지입니다."
        ).judge();

    }
}
