package com.dockerinit.features.dockercompose.renderer.impl;

import com.dockerinit.features.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.dockercompose.domain.Service;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ComposeEnvRendererTest {

    private final ComposeEnvRenderer renderer = new ComposeEnvRenderer();

    private static RenderContext<ComposeRequestV1, ComposePlan> ctx(ComposePlan plan, FileType... targets) {
        return new RenderContext<>(null, plan, EnumSet.copyOf(Arrays.asList(targets)), List.of());
    }

    @Test
    @DisplayName("target 에 ENV 가 포함되어 있을때만 supports ->  true 리턴하나? ")
    void return_true_when_targets_contains_env() {
        ComposePlan plan = new ComposePlan("app", List.of(), Map.of(), Map.of(), List.of());
        assertThat(renderer.supports(ctx(plan, FileType.COMPOSE, FileType.README, FileType.MANIFEST))).isFalse();
        assertThat(renderer.supports(ctx(plan, FileType.ENV))).isTrue();
        assertThat(renderer.supports(ctx(plan, FileType.ENV, FileType.COMPOSE, FileType.README, FileType.MANIFEST))).isTrue();
    }

    @Test
    @DisplayName(".env.example: 서비스 env 유니온 + 충돌키는 빈값으로, 경고 포함")
    void env_union_and_conflict() {
        Service a = new Service(
                "api", "alpine", null,
                List.of(), Map.of("PORT","8080","PROFILE","dev"),
                List.of(), List.of(), List.of(), List.of(),
                null, null
        );
        Service b = new Service(
                "web", "nginx", null,
                List.of(), Map.of("PORT","80","HOST","0.0.0.0"),
                List.of(), List.of(), List.of(), List.of(),
                null, null
        );
        ComposePlan plan = new ComposePlan("proj", List.of(a,b), Map.of(), Map.of(), List.of());

        var warnings = new ArrayList<String>();
        var out = renderer.render(ctx(plan, FileType.ENV), warnings);
        assertThat(out).hasSize(1);
        GeneratedFile f = out.get(0);

        String env = new String(f.content(), StandardCharsets.UTF_8);
        // 충돌한 PORT는 빈값으로
        assertThat(env).contains("\nPORT=\n");
        // 나머지는 채워짐
        assertThat(env).contains("\nHOST=0.0.0.0\n");
        assertThat(env).contains("\nPROFILE=dev\n");

        // 경고 포함
        assertThat(warnings).anySatisfy(w -> assertThat(w).contains(".env", "레포지토리", "env-file"));

        // 민감 플래그
        assertThat(f.sensitive()).isTrue();
    }

    @Test
    @DisplayName("services에 env가 없으면 안내 코멘트만 생성")
    void env_empty_comment_only() {
        Service a = new Service("svc", "alpine", null,
                List.of(), Map.of(), List.of(), List.of(), List.of(), List.of(), null, null);
        ComposePlan plan = new ComposePlan("p", List.of(a), Map.of(), Map.of(), List.of());

        var warnings = new ArrayList<String>();
        var out = renderer.render(ctx(plan, FileType.ENV), warnings);
        String env = new String(out.get(0).content(), StandardCharsets.UTF_8);

        assertThat(env).contains("# No environment variables declared");
        assertThat(env).contains("Example .env");
    }



}