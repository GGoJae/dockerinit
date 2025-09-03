package com.dockerinit.features.dockercompose.renderer.impl;

import com.dockerinit.features.dockercompose.domain.composeCustom.Build;
import com.dockerinit.features.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.dockercompose.domain.Service;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.RenderContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ComposeReadmeRendererTest {

    private final ComposeReadmeRenderer renderer = new ComposeReadmeRenderer();

    private static RenderContext<ComposeRequestV1, ComposePlan> ctx(ComposePlan plan, FileType... targets) {
        return new RenderContext<>(null, plan, EnumSet.copyOf(Arrays.asList(targets)), List.of());
    }

    @Test
    @DisplayName("supports: README 타겟일 때만 true")
    void supports_by_target() {
        var plan = new ComposePlan("p", List.of(), Map.of(), Map.of(), List.of());
        assertThat(renderer.supports(ctx(plan, FileType.README))).isTrue();
        assertThat(renderer.supports(ctx(plan, FileType.COMPOSE))).isFalse();
    }

    @Test
    @DisplayName("README: 프로젝트명/서비스 목록/기본 명령 포함")
    void readme_basic() {
        Service s1 = new Service("web", "nginx", null,
                List.of(), Map.of(), List.of(), List.of("8080:80"), List.of(), List.of(),
                null, null);
        Service s2 = new Service("db", null, new Build(".", "Dockerfile", Map.of()),
                List.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                null, null);
        ComposePlan plan = new ComposePlan("myproj", List.of(s1, s2), Map.of(), Map.of(), List.of());

        var warnings = new ArrayList<String>();
        var out = renderer.render(ctx(plan, FileType.README), warnings);
        String md = new String(out.get(0).content(), StandardCharsets.UTF_8);

        log.info("this is md : {}", md);
        assertThat(md).contains("Docker Compose - myproj");
        assertThat(md).containsAnyOf("**Services**: web, db", "**Services**: db, web"); // 목록 요약
        // build 단계 안내
        assertThat(md).contains("docker compose build");
        // env-file 안내(기본 포함)
        assertThat(md).contains("--env-file .env");
        // 포트/볼륨 섹션 중 포트 요약
        assertThat(md).contains("## Ports").contains("web: 8080:80");
    }
}