package com.dockerinit.features.dockercompose.renderer.impl;

import com.dockerinit.features.dockercompose.domain.*;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ComposeRendererTest {

    private final ComposeRenderer renderer = new ComposeRenderer();

    private static RenderContext<ComposeRequestV1, ComposePlan> ctx(ComposePlan plan, FileType... targets) {
        return new RenderContext<>(null, plan, EnumSet.copyOf(Arrays.asList(targets)), List.of());
    }

    private static String renderToString(ComposeRenderer r, ComposePlan plan) {
        List<String> warnings = new ArrayList<>();
        List<GeneratedFile> render = r.render(ctx(plan, FileType.COMPOSE), warnings);
        assertThat(render).hasSize(1);
        GeneratedFile file = render.get(0);
        return new String(file.content(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("기본: version과 services 섹션, 단일 서비스 image 출력")
    void basic_image_service() {
        Service s = new Service(
                "web",
                "nginx:1.27",
                null,
                List.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                null, null
        );
        ComposePlan plan = new ComposePlan("demo", List.of(s), Map.of(), Map.of(), List.of());

        String yml = renderToString(renderer, plan);
        assertThat(yml).contains("version: \"3.9\"\n");
        assertThat(yml).contains("services:\n");
        assertThat(yml).contains("  web:\n");
        // compose 렌더러는 단일 인용부호로 감싸도록 되어 있음
        assertThat(yml).contains("    image: 'nginx:1.27'\n");
    }

    @Test
    @DisplayName("build: context/dockerfile/args(키 정렬) 출력")
    void build_with_args_sorted() {
        Build b = new Build(".", "Dockerfile", Map.of("Z", "9", "A", "1"));
        Service s = new Service(
                "api",
                null, b,
                List.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                null, null
        );
        ComposePlan plan = new ComposePlan("app", List.of(s), Map.of(), Map.of(), List.of());

        String yml = renderToString(renderer, plan);
        assertThat(yml).contains("""
            build:
              context: '.'
              dockerfile: 'Dockerfile'
              args:
                A: '1'
                Z: '9'
        """.replace("\r",""));

    }

    @Test
    @DisplayName("command: 리스트는 [ 'a', 'b', 'c' ] 형태로 출력")
    void command_list() {
        Service s = new Service(
                "job",
                "alpine:3.20",
                null,
                List.of("sh", "-c", "echo hi"),
                Map.of(), List.of(), List.of(), List.of(), List.of(),
                null, null
        );
        ComposePlan plan = new ComposePlan("app", List.of(s), Map.of(), Map.of(), List.of());

        String yml = renderToString(renderer, plan);
        assertThat(yml).contains("    command: ['sh', '-c', 'echo hi']\n");
    }

    @Test
    @DisplayName("environment: 키 정렬 및 값 인용")
    void environment_sorted_and_quoted() {
        Service s = new Service(
                "svc",
                "busybox",
                null,
                List.of(),
                Map.of("Z","9","A","1"),
                List.of(), List.of(), List.of(), List.of(),
                null, null
        );
        ComposePlan plan = new ComposePlan("app", List.of(s), Map.of(), Map.of(), List.of());

        String yml = renderToString(renderer, plan);
        // A 먼저, Z 나중
        assertThat(yml).contains("""
                    environment:
                      A: '1'
                      Z: '9'
                """.replace("\r",""));
    }

    @Test
    @DisplayName("ports/volumes/depends_on/restart 출력")
    void ports_volumes_depends_restart() {
        Service s = new Service(
                "web",
                "nginx",
                null,
                List.of(),
                Map.of(),
                List.of(".env"),
                List.of("8080:80", "443:443"),
                List.of("./data:/data"),
                List.of("db"),
                "always",
                null
        );
        ComposePlan plan = new ComposePlan("app", List.of(s), Map.of(), Map.of(), List.of());

        String yml = renderToString(renderer, plan);
        assertThat(yml).contains("    env_file:\n      - '.env'\n");
        assertThat(yml).contains("    ports:\n      - '8080:80'\n      - '443:443'\n");
        assertThat(yml).contains("    volumes:\n      - './data:/data'\n");
        assertThat(yml).contains("    depends_on:\n      - 'db'\n");
        assertThat(yml).contains("    restart: 'always'\n");
    }

    @Test
    @DisplayName("healthcheck: test 및 옵션들 출력(start_period 포함)")
    void healthcheck() {
        Healthcheck hc = new Healthcheck("curl --fail http://web:80 || exit 1", "30s", "5s", 3, "10s");
        Service s = new Service(
                "web", "nginx", null,
                List.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                null, hc
        );
        ComposePlan plan = new ComposePlan("app", List.of(s), Map.of(), Map.of(), List.of());

        String yml = renderToString(renderer, plan);
        assertThat(yml).contains("""
                    healthcheck:
                      test: 'curl --fail http://web:80 || exit 1'
                      interval: 30s
                      timeout: 5s
                      retries: 3
                      start_period: 10s
                """.replace("\r",""));
    }

    @Test
    @DisplayName("top-level networks/volumes 출력")
    void top_level_networks_and_volumes() {
        Map<String, Network> nets = Map.of("mynet", new Network("bridge"));
        Map<String, Volume> vols = Map.of("datavol", new Volume("local"));
        ComposePlan plan = new ComposePlan("app", List.of(), nets, vols, List.of());

        String yml = renderToString(renderer, plan);
        assertThat(yml).contains("""
                networks:
                  mynet:
                    driver: 'bridge'
                """.replace("\r",""));
        assertThat(yml).contains("""
                volumes:
                  datavol:
                    driver: 'local'
                """.replace("\r",""));
    }

}