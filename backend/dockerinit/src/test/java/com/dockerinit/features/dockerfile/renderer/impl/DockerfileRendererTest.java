package com.dockerinit.features.dockerfile.renderer.impl;

import com.dockerinit.features.dockerfile.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DockerfileRendererTest {

    private final DockerfileRenderer renderer = new DockerfileRenderer();

    private static String render(DockerfilePlan plan) {
        RenderContext ctx = new RenderContext(null, plan, EnumSet.of(FileType.DOCKERFILE), List.of());
        List<GeneratedFile> out = new DockerfileRenderer().render(ctx, new ArrayList<>());
        return new String(out.get(0).content(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("빈/누락 필드는 생략되고 FROM 한 줄만 출력된다")
    void only_from_when_others_empty() {
        DockerfilePlan plan = new DockerfilePlan(
                "alpine:3.20",
                null,                // workdir
                List.of(),           // copy
                List.of(),           // add
                EnvMode.DEV,
                Map.of(),            // env
                List.of(),           // expose
                List.of(),           // cmd
                List.of(),           // run
                List.of(),           // entrypoint
                Map.of(),            // label
                null,                // user
                Map.of(),            // args
                null,                // healthcheck
                List.of(),           // volume,
                new ArrayList<>(),
                Set.of()
        );

        String text = render(plan);
        assertThat(text).isEqualTo("FROM alpine:3.20");
    }

    @Test
    @DisplayName("DEV_LIKE: ENV 인라인, LABEL/ARG 키 정렬, JSON 배열/이스케이프, 기타 섹션 렌더")
    void dev_like_inlines_env_and_sorts() {
        DockerfilePlan plan = new DockerfilePlan(
                "eclipse-temurin:17-jre",         // baseImage
                "/app",                           // workdir
                List.of(new CopyEntry("src/", "/app/src")), // copy
                List.of(new CopyEntry("file with space.txt", "/app/space file.txt")), // add
                EnvMode.DEV,                 // envMode
                Map.of("B", "2", "A", "1"),       // envVars
                List.of(8081, 8080),              // expose
                List.of("java", "-jar", "app.jar"), // cmd (불변식 충족)
                List.of("apt-get update"),        // run
                List.of("sh","-c"),               // entrypoint
                Map.of("maintainer","me","app","demo"), // label
                "1000:1000",                      // user
                Map.of("Z", "", "A", "x"),        // args
                new HealthcheckSpec("CMD curl -f http://localhost:8080/actuator/health || exit 1", "30s","3s",3,"10s"), // health
                List.of("/data"),                 // volume
                List.of(),                        // warnings
                EnumSet.of(FileType.DOCKERFILE)   // targets
        );

        String text = render(plan);

        assertThat(text).contains("FROM eclipse-temurin:17-jre");
        assertThat(text).contains("WORKDIR /app");
        // LABEL 정렬(app -> maintainer)
        assertThat(text).containsSubsequence("LABEL app=\"demo\"", "LABEL maintainer=\"me\"");
        // ARG 정렬 + 값 없는 ARG는 키만
        assertThat(text).containsSubsequence("ARG A=x", "ARG Z");
        // ENV 실제 값 인라인
        assertThat(text).containsSubsequence("ENV A=\"1\"", "ENV B=\"2\"");
        // ADD/COPY JSON 배열 & 이스케이프
        assertThat(text).contains("ADD [\"file with space.txt\",\"/app/space file.txt\"]");
        assertThat(text).contains("COPY [\"src/\",\"/app/src\"]");
        // RUN
        assertThat(text).contains("RUN apt-get update");
        // USER
        assertThat(text).contains("USER 1000:1000");
        // VOLUME
        assertThat(text).contains("VOLUME [\"/data\"]");
        // EXPOSE 정렬
        assertThat(text).containsSubsequence("EXPOSE 8080", "EXPOSE 8081");
        // ENTRYPOINT/CMD JSON 배열
        assertThat(text).contains("ENTRYPOINT [\"sh\",\"-c\"]");
        assertThat(text).contains("CMD [\"java\",\"-jar\",\"app.jar\"]");
        // HEALTHCHECK (옵션/접두어 유지)
        assertThat(text).contains("HEALTHCHECK --interval=30s --timeout=3s --retries=3 --start-period=10s CMD curl -f http://localhost:8080/actuator/health || exit 1");
        // 마지막 개행 없음
        assertThat(text).doesNotEndWith("\n");
    }

    @Test
    @DisplayName("PROD_LIKE: ENV는 플레이스홀더(${KEY})로 렌더되고 경고 주석이 추가된다")
    void prod_like_env_placeholders_and_warning() {
        DockerfilePlan plan = new DockerfilePlan(
                "alpine:3.20",
                null,
                List.of(), List.of(),
                EnvMode.PROD_LIKE,
                Map.of("SPRING_PROFILES_ACTIVE","prod","SECRET","s3cr3t"),
                List.of(),
                List.of("sh","-c"), // 최소 CMD/ENTRYPOINT 중 하나 필요 → 여기서는 CMD
                List.of(),
                List.of(),
                Map.of(),
                null,
                Map.of(),
                null,
                List.of(),
                List.of(),
                EnumSet.of(FileType.DOCKERFILE)
        );

        String text = render(plan);
        assertThat(text).contains("# 환경 변수는 런타임시에 주입해주세요");
        assertThat(text).contains("ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}");
        assertThat(text).contains("ENV SECRET=${SECRET}");
    }

    @Test
    @DisplayName("COPY/ADD/ENV/ENTRYPOINT/CMD는 JSON 포맷과 이스케이프가 정확하다")
    void json_and_escaping_are_correct() {
        DockerfilePlan plan = new DockerfilePlan(
                "alpine:3.20",
                "/w",
                List.of(new CopyEntry("sp ace.txt", "/w/space file.txt")),
                List.of(new CopyEntry("a\"b", "/w/c\"d")),
                EnvMode.DEV,
                Map.of("K", "va\"l\\ue"),
                List.of(),
                List.of("sh","-c","echo \"hi\""), // cmd
                List.of(),
                List.of("sh","-c"),               // entrypoint
                Map.of(),
                null,
                Map.of(),
                null,
                List.of(),
                List.of(),
                EnumSet.of(FileType.DOCKERFILE)
        );

        String text = render(plan);
        // ENV 이스케이프
        assertThat(text).contains("ENV K=\"va\\\"l\\\\ue\"");
        // COPY/ADD JSON 배열 & 이스케이프
        assertThat(text).contains("COPY [\"sp ace.txt\",\"/w/space file.txt\"]");
        assertThat(text).contains("ADD [\"a\\\"b\",\"/w/c\\\"d\"]");
        // ENTRYPOINT/CMD JSON 배열
        assertThat(text).contains("ENTRYPOINT [\"sh\",\"-c\"]");
        assertThat(text).contains("CMD [\"sh\",\"-c\",\"echo \\\"hi\\\"\"]");
    }

    @Test
    @DisplayName("EXPOSE는 항상 오름차순으로 정렬된다")
    void expose_is_sorted() {
        DockerfilePlan plan = new DockerfilePlan(
                "alpine:3.20",
                null,
                List.of(), List.of(),
                EnvMode.DEV,
                Map.of(),
                List.of(9000, 80, 8080), // expose
                List.of("sh","-c"),      // cmd(최소)
                List.of(), List.of(),
                Map.of(), null, Map.of(), null,
                List.of(),
                List.of(),
                EnumSet.of(FileType.DOCKERFILE)
        );

        String text = render(plan);
        assertThat(text).containsSubsequence("EXPOSE 80", "EXPOSE 8080", "EXPOSE 9000");
    }

    @Test
    @DisplayName("HEALTHCHECK: 옵션 결합 및 CMD 접두어 자동 추가")
    void healthcheck_options_and_cmd_prefix_added() {
        DockerfilePlan plan = new DockerfilePlan(
                "alpine:3.20",
                null,
                List.of(), List.of(),
                EnvMode.DEV,
                Map.of(), List.of(),
                List.of("sh","-c"), // cmd(최소)
                List.of(), List.of(),
                Map.of(), null, Map.of(),
                new HealthcheckSpec("curl -f http://localhost || exit 1", "10s","2s",2,"5s"),
                List.of(),
                List.of(),
                EnumSet.of(FileType.DOCKERFILE)
        );

        String text = render(plan);
        assertThat(text).contains("HEALTHCHECK --interval=10s --timeout=2s --retries=2 --start-period=5s CMD curl -f http://localhost || exit 1");
    }

    @Test
    @DisplayName("RUN 여러 줄은 순서대로 각 한 줄씩 렌더링된다")
    void run_lines_rendered_as_is() {
        DockerfilePlan plan = new DockerfilePlan(
                "alpine:3.20",
                null,
                List.of(), List.of(),
                EnvMode.DEV,
                Map.of(), List.of(),
                List.of("sh","-c"), // cmd(최소)
                List.of("apk add --no-cache curl", "echo done"),
                List.of(),
                Map.of(), null, Map.of(), null,
                List.of(),
                List.of(),
                EnumSet.of(FileType.DOCKERFILE)
        );

        String text = render(plan);
        assertThat(text).containsSubsequence(
                "RUN apk add --no-cache curl",
                "RUN echo done"
        );
    }

    @Test
    @DisplayName("supports는 항상 true이며 fileType은 DOCKERFILE이다")
    void supports_always_true_and_filetype_is_dockerfile() {
        DockerfilePlan plan = new DockerfilePlan(
                "alpine:3.20",
                null,
                List.of(), List.of(),
                EnvMode.DEV,
                Map.of(), List.of(),
                List.of("echo","ok"), // cmd(최소)
                List.of(), List.of(),
                Map.of(), null, Map.of(), null,
                List.of(),
                List.of(),
                EnumSet.of(FileType.DOCKERFILE)
        );

        RenderContext ctx = new RenderContext(null, plan, EnumSet.of(FileType.DOCKERFILE), List.of());
        assertThat(renderer.supports(ctx)).isTrue();
        assertThat(renderer.fileType()).isEqualTo(FileType.DOCKERFILE);
    }

    @Test
    @DisplayName("불변식: CMD와 ENTRYPOINT가 모두 비면 warning 에 추가된다")
    void invariant_cmd_or_entrypoint_required() {
        DockerfilePlan plan = new DockerfilePlan(
                "alpine:3.20",
                null,
                List.of(), List.of(),
                EnvMode.DEV,
                Map.of(), List.of(),
                List.of(),            // cmd 비움
                List.of(),            // run
                List.of(),            // entrypoint 비움
                Map.of(), null, Map.of(), null,
                List.of(),
                new ArrayList<>(),
                EnumSet.of(FileType.DOCKERFILE)
        );

        assertThat(plan.warnings()).hasSize(1);
        assertThat(plan.warnings()).contains("No CMD/ENTRYPOINT: container won't start without a command.");

    }



}