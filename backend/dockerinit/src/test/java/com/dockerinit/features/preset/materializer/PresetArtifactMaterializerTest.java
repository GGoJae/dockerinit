package com.dockerinit.features.preset.materializer;

import com.dockerinit.features.application.preset.materializer.PresetArtifactMaterializer;
import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.application.preset.domain.ContentStrategy;
import com.dockerinit.features.application.preset.domain.PresetArtifact;
import com.dockerinit.global.exception.UnsupportedOperationCustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class PresetArtifactMaterializerTest {

    private final PresetArtifactMaterializer materializer = new PresetArtifactMaterializer();

    private static PresetArtifact embedded(FileType type, String name, ContentType ct, String content, boolean sensitive) {
        return PresetArtifact.builder()
                .fileType(type)
                .filename(name)
                .contentType(ct)
                .strategy(ContentStrategy.EMBEDDED)
                .embeddedContent(content)
                .sensitive(sensitive)
                .build();
    }

    private static PresetArtifact objectStored(FileType type, String name, ContentType ct, String storageKey) {
        return PresetArtifact.builder()
                .fileType(type)
                .filename(name)
                .contentType(ct)
                .strategy(ContentStrategy.OBJECT_STORAGE)
                .storageKey(storageKey)
                .build();
    }

    @Test
    @DisplayName("EMBEDDED 아티팩트 → GeneratedFile 변환 및 필드 보존")
    void embedded_to_generatedFile_ok() {
        PresetArtifact a = embedded(FileType.DOCKERFILE, "Dockerfile", ContentType.TEXT, "FROM alpine\n", true);

        List<GeneratedFile> out = materializer.toGeneratedFiles(List.of(a), Set.of(FileType.DOCKERFILE));
        assertThat(out).hasSize(1);

        GeneratedFile g = out.get(0);
        assertThat(g.filename()).isEqualTo("Dockerfile");
        assertThat(g.fileType()).isEqualTo(FileType.DOCKERFILE);
        assertThat(g.contentType()).isEqualTo(ContentType.TEXT);
        assertThat(g.sensitive()).isTrue();
        assertThat(new String(g.content(), StandardCharsets.UTF_8)).isEqualTo("FROM alpine\n");
    }

    @Test
    @DisplayName("타겟 필터링: 대상에 없는 fileType은 제외된다")
    void target_filtering_excludes_non_targets() {
        PresetArtifact readme = embedded(FileType.README, "README.md", ContentType.MD, "# hi\n", false);
        PresetArtifact dockerfile = embedded(FileType.DOCKERFILE, "Dockerfile", ContentType.TEXT, "FROM busybox\n", false);

        List<GeneratedFile> out = materializer.toGeneratedFiles(List.of(readme, dockerfile), Set.of(FileType.README));
        assertThat(out).hasSize(1);
        assertThat(out.get(0).filename()).isEqualTo("README.md");
        assertThat(out.get(0).fileType()).isEqualTo(FileType.README);
    }

    @Test
    @DisplayName("입력 순서 유지: 필터 후에도 순서는 입력 순서를 따른다")
    void preserves_input_order_after_filter() {
        PresetArtifact a = embedded(FileType.README, "A.md", ContentType.MD, "A", false);
        PresetArtifact b = embedded(FileType.ENV, ".env.example", ContentType.TEXT, "K=V", false);
        PresetArtifact c = embedded(FileType.DOCKERFILE, "Dockerfile", ContentType.TEXT, "FROM x", false);

        List<GeneratedFile> out = materializer.toGeneratedFiles(List.of(a, b, c), Set.of(FileType.DOCKERFILE, FileType.README));
        assertThat(out).extracting(GeneratedFile::filename).containsExactly("A.md", "Dockerfile");
    }

    @Test
    @DisplayName("OBJECT_STORAGE 전략은 아직 미지원 예외")
    void object_storage_not_supported() {
        PresetArtifact obj = objectStored(FileType.DOCKERFILE, "Dockerfile", ContentType.TEXT, "presets/foo/Dockerfile");

        assertThatThrownBy(() ->
                materializer.toGeneratedFiles(List.of(obj), Set.of(FileType.DOCKERFILE))
        ).isInstanceOf(UnsupportedOperationCustomException.class)
                .hasMessageContaining("OBJECT_STORAGE");
    }

    @Test
    @DisplayName("EMBEDDED인데 content가 없으면 NPE")
    void embedded_without_content_throws_npe() {
        PresetArtifact broken = PresetArtifact.builder()
                .fileType(FileType.DOCKERFILE)
                .filename("Dockerfile")
                .contentType(ContentType.TEXT)
                .strategy(ContentStrategy.EMBEDDED)
                .embeddedContent(null) // <- 결핍
                .build();

        assertThatThrownBy(() ->
                materializer.toGeneratedFiles(List.of(broken), Set.of(FileType.DOCKERFILE))
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("빈 입력은 빈 결과")
    void empty_input_returns_empty() {
        List<GeneratedFile> out = materializer.toGeneratedFiles(List.of(), Set.of(FileType.DOCKERFILE));
        assertThat(out).isEmpty();
    }

    @Test
    @DisplayName("UTF-8 컨텐츠 보존 (한글/특수문자)")
    void utf8_content_preserved() {
        String content = "여기=값\n특수문자: ©✓\n";
        PresetArtifact a = embedded(FileType.ENV, ".env.example", ContentType.TEXT, content, false);

        List<GeneratedFile> out = materializer.toGeneratedFiles(List.of(a), Set.of(FileType.ENV));
        assertThat(out).hasSize(1);
        assertThat(new String(out.get(0).content(), StandardCharsets.UTF_8)).isEqualTo(content);
    }
}
