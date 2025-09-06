package com.dockerinit.features.preset.mapper;

import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.preset.domain.*;
import com.dockerinit.features.preset.dto.request.PresetArtifactRequest;
import com.dockerinit.features.preset.dto.request.PresetCreateRequest;
import com.dockerinit.features.preset.dto.request.PresetUpdateRequest;
import com.dockerinit.features.preset.dto.response.PresetArtifactMetaResponse;
import com.dockerinit.features.preset.dto.response.PresetDetailResponse;
import com.dockerinit.features.preset.dto.response.PresetSummaryResponse;
import com.dockerinit.features.preset.dto.spec.ContentStrategyDTO;
import com.dockerinit.features.preset.dto.spec.EnvValueModeDTO;
import com.dockerinit.features.preset.dto.spec.PresetKindDTO;
import com.dockerinit.features.preset.dto.spec.RenderPolicyDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PresetMapperTest {

    @Test
    @DisplayName("createDocument: 기본 필드 트리밍/정규화 및 값 매핑")
    void createDocument_basic() {
        String slug = "df--java-spring-v1";
        PresetCreateRequest req = new PresetCreateRequest(
                /* slug (있어도 무시해도 됨) */ List.of(" willBeIgnored "),
                /* displayName */ "  Spring Boot JDK17  ",
                /* description */ "  desc  ",
                /* presetKind */ PresetKindDTO.DOCKERFILE,
                /* tags */ setOf("  Lang:Java ", "FRAMEWORK:Spring-Boot"),
                /* schemaVersion */ 1,
                /* renderPolicy */ new RenderPolicyDTO(EnvValueModeDTO.PLACEHOLDER, "${%s}", true, true, "LF"),
                /* artifacts */ List.of(
                new PresetArtifactRequest(
                        FileType.DOCKERFILE, "Dockerfile", "text/plain; charset=utf-8",
                        ContentStrategyDTO.EMBEDDED, "FROM eclipse-temurin:17-jre\n", null, null,
                        false, null, 23L
                ),
                new PresetArtifactRequest(
                        FileType.README, "README.md", "text/markdown; charset=utf-8",
                        ContentStrategyDTO.EMBEDDED, "# howto", null, null,
                        false, null, 7L
                )
        ),
                /* defaultTargets */ setOf(FileType.DOCKERFILE, FileType.README),
                /* instructions */ "  use this  ",
                /* active */ true,
                /* deprecated */ null,
                /* deprecationNote */ "  "
        );

        PresetDocument doc = PresetMapper.createDocument(slug, req, "GJ");

        assertThat(doc.getSlug()).isEqualTo(slug);
        assertThat(doc.getDisplayName()).isEqualTo("Spring Boot JDK17");
        assertThat(doc.getDescription()).isEqualTo("desc");
        assertThat(doc.getPresetKind()).isEqualTo(PresetKind.DOCKERFILE);
        assertThat(doc.getTags()).containsExactlyInAnyOrder("lang:java", "framework:spring-boot");
        assertThat(doc.getSchemaVersion()).isEqualTo(1);
        assertThat(doc.getRenderPolicy()).isNotNull();
        assertThat(doc.getArtifacts()).hasSize(2);
        assertThat(doc.getDefaultTargets()).containsExactlyInAnyOrder(FileType.DOCKERFILE, FileType.README);
        assertThat(doc.getInstructions()).isEqualTo("use this");
        assertThat(doc.getActive()).isTrue();
        assertThat(doc.getDeprecated()).isFalse(); // null -> false로 맵핑하지 않았다면 이 검증은 제거/수정
        assertThat(doc.getDeprecationNote()).isNull();
        assertThat(doc.getCreatedBy()).isEqualTo("GJ");
        assertThat(doc.getUpdatedBy()).isEqualTo("GJ");
    }


    /* ---------------- mapArtifactToDomain validation ---------------- */

    @Test
    @DisplayName("mapArtifactToDomain: EMBEDDED는 inlineContent 필수")
    void embedded_requires_inline() {
        PresetArtifactRequest a = new PresetArtifactRequest(
                FileType.DOCKERFILE, "Dockerfile", "text/plain",
                ContentStrategyDTO.EMBEDDED, /* inline */ null,
                null, null, null, null, null
        );
        assertThatThrownBy(() -> PresetMapper.mapArtifactToDomain(a))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inlineContent");
    }

    @Test
    @DisplayName("mapArtifactToDomain: OBJECT_STORAGE는 storageKey 필수")
    void object_storage_requires_key() {
        PresetArtifactRequest a = new PresetArtifactRequest(
                FileType.DOCKERFILE, "Dockerfile", "text/plain",
                ContentStrategyDTO.OBJECT_STORAGE, /* inline */ null,
                "s3", /* storageKey */ null, null, null, null
        );
        assertThatThrownBy(() -> PresetMapper.mapArtifactToDomain(a))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("storageKey");
    }

    /* ---------------- toContentType / normalizeEtag ---------------- */

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource({
            "text/yaml, YAML",
            "application/x-yaml, YAML",
            "application/yml, YAML",
            "text/json, JSON",
            "application/json, JSON",
            "text/markdown, MD",
            "text/x-markdown, MD",
            "application/zip, ZIP",
            "text/plain, TEXT",
            "application/octet-stream, OCTET",
            "'', OCTET",
            "   , OCTET"
    })
    @DisplayName("toContentType: MIME 문자열 매핑")
    void toContentType_mapping(String mime, String expectedToken) {
        var actual = PresetMapper.toContentType(mime);
        assertThat(actual).isSameAs(ct(expectedToken));
    }

    private static com.dockerinit.features.model.ContentType ct(String token) {
        return switch (token) {
            case "YAML"  -> com.dockerinit.features.model.ContentType.YAML;
            case "JSON"  -> com.dockerinit.features.model.ContentType.JSON;
            case "MD"    -> com.dockerinit.features.model.ContentType.MD;
            case "ZIP"   -> com.dockerinit.features.model.ContentType.ZIP;
            case "TEXT"  -> com.dockerinit.features.model.ContentType.TEXT;
            case "OCTET" -> com.dockerinit.features.model.ContentType.OCTET;
            default -> throw new IllegalArgumentException("unknown token: " + token);
        };
    }

    @Test
    @DisplayName("normalizeEtag: 양끝 따옴표 제거")
    void normalizeEtag_quotes() {
        assertThat(PresetMapper.normalizeEtag("\"abc\"")).isEqualTo("abc");
        assertThat(PresetMapper.normalizeEtag("\"012345\"")).isEqualTo("012345");
        assertThat(PresetMapper.normalizeEtag("noquotes")).isEqualTo("noquotes");
        assertThat(PresetMapper.normalizeEtag(null)).isNull();
        assertThat(PresetMapper.normalizeEtag("")).isNull();
        assertThat(PresetMapper.normalizeEtag("   ")).isNull();
    }

    /* ---------------- toDetail / toSummary ---------------- */

    @Test
    @DisplayName("toDetail / toSummary: 도메인→응답 DTO 변환")
    void toDetail_toSummary() {
        PresetDocument doc = PresetDocument.builder()
                .id("id1")
                .slug("df--java-spring-v1")
                .displayName("Spring")
                .description("desc")
                .presetKind(PresetKind.DOCKERFILE)
                .tags(Set.of("lang:java"))
                .schemaVersion(1)
                .renderPolicy(RenderPolicy.builder()
                        .envValueMode(EnvValueMode.PLACEHOLDER)
                        .placeholderFormat("${%s}")
                        .includeManifestByDefault(true)
                        .ensureTrailingNewline(true)
                        .lineEndings("LF")
                        .build()
                )
                .artifacts(List.of(
                        PresetArtifact.builder()
                                .fileType(FileType.DOCKERFILE)
                                .filename("Dockerfile")
                                .contentType(ContentType.TEXT)
                                .strategy(ContentStrategy.EMBEDDED)
                                .embeddedContent("FROM eclipse-temurin:17-jre")
                                .sensitive(false)
                                .build()
                ))
                .defaultTargets(Set.of(FileType.DOCKERFILE))
                .instructions("how to")
                .active(true)
                .deprecated(false)
                .deprecationNote(null)
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-02T00:00:00Z"))
                .downloadCount(42L)
                .build();

        PresetDetailResponse detail = PresetMapper.toDetail(doc);
        assertThat(detail.slug()).isEqualTo("df--java-spring-v1");
        assertThat(detail.presetKind()).isEqualTo(PresetKindDTO.DOCKERFILE);
        assertThat(detail.renderPolicy()).isNotNull();
        assertThat(detail.artifacts()).hasSize(1);
        PresetSummaryResponse sum = PresetMapper.toSummary(doc);
        assertThat(sum.slug()).isEqualTo("df--java-spring-v1");
        assertThat(sum.presetKind()).isEqualTo(PresetKindDTO.DOCKERFILE);
        assertThat(sum.downloadCount()).isEqualTo(42L);
        assertThat(sum.updatedAt()).isEqualTo(doc.getUpdatedAt());
    }

    /* ---------------- merge ---------------- */

    @Test
    @DisplayName("merge: null인 필드는 유지, 값이 있는 필드만 갱신")
    void merge_updates_only_non_null() {
        PresetDocument base = PresetDocument.builder()
                .slug("df--java-spring-v1")
                .displayName("Spring")
                .description("desc")
                .presetKind(PresetKind.DOCKERFILE)
                .tags(Set.of("lang:java"))
                .schemaVersion(1)
                .renderPolicy(RenderPolicy.builder().envValueMode(EnvValueMode.INLINE).build())
                .artifacts(List.of(
                        PresetArtifact.builder()
                                .fileType(FileType.README)
                                .filename("README.md")
                                .contentType(ContentType.MD)
                                .strategy(ContentStrategy.EMBEDDED)
                                .embeddedContent("# readme")
                                .build()
                ))
                .defaultTargets(Set.of(FileType.DOCKERFILE, FileType.README))
                .instructions("how to")
                .active(true)
                .deprecated(false)
                .build();

        PresetUpdateRequest req = new PresetUpdateRequest(
                /* displayName */ " Spring Boot  ",
                /* description */ null,
                /* presetKind  */ PresetKindDTO.BUNDLE,
                /* tags        */ Set.of(" Lang:Java ", " Cloud:Aws "),
                /* schemaVer   */ 2,
                /* renderPol   */ new RenderPolicyDTO(EnvValueModeDTO.PLACEHOLDER, "${%s}", true, false, "CRLF"),
                /* artifacts   */ List.of(
                new PresetArtifactRequest(
                        FileType.MANIFEST, "manifest.json", "application/json",
                        ContentStrategyDTO.EMBEDDED, "{\"k\":1}", null, null, null, null, 8L
                )
        ),
                /* defaultTargets */ Set.of(FileType.MANIFEST),
                /* instructions   */ "  updated how to ",
                /* active         */ null,
                /* deprecated     */ true,
                /* deprecNote     */ "  planned sunset "
        );

        PresetDocument merged = PresetMapper.merge(base, req, "UPDATER");

        assertThat(merged.getDisplayName()).isEqualTo("Spring Boot");
        assertThat(merged.getDescription()).isEqualTo("desc"); // null 업데이트는 유지
        assertThat(merged.getPresetKind()).isEqualTo(PresetKind.BUNDLE);
        assertThat(merged.getTags()).containsExactlyInAnyOrder("lang:java", "cloud:aws");
        assertThat(merged.getSchemaVersion()).isEqualTo(2);
        assertThat(merged.getRenderPolicy()).isNotNull();
        assertThat(merged.getArtifacts()).singleElement().satisfies(a -> {
            assertThat(a.getFileType()).isEqualTo(FileType.MANIFEST);
            assertThat(a.getContentType()).isEqualTo(ContentType.JSON);
        });
        assertThat(merged.getDefaultTargets()).containsExactly(FileType.MANIFEST);
        assertThat(merged.getInstructions()).isEqualTo("updated how to");
        assertThat(merged.getActive()).isTrue(); // null -> 유지
        assertThat(merged.getDeprecated()).isTrue();
        assertThat(merged.getDeprecationNote()).isEqualTo("planned sunset");
        assertThat(merged.getUpdatedBy()).isEqualTo("UPDATER");
    }

    /* ---------------- mapArtifactToRes ---------------- */

    @Test
    @DisplayName("mapArtifactToRes: ContentType/ETag 변환 및 메타 필드 매핑")
    void mapArtifactToRes_ok() {
        PresetArtifact art = PresetArtifact.builder()
                .fileType(FileType.DOCKERFILE)
                .filename("Dockerfile")
                .contentType(ContentType.TEXT)
                .strategy(ContentStrategy.EMBEDDED)
                .embeddedContent("FROM alpine:3.20")
                .sensitive(true)
                .etag("\"abcdef\"")
                .contentLength(123L)
                .storageProvider("s3")
                .storageKey("presets/df--alpine/Dockerfile")
                .build();

        PresetArtifactMetaResponse meta = PresetMapper.mapArtifactToRes(art);
        assertThat(meta.fileType()).isEqualTo(FileType.DOCKERFILE);
        assertThat(meta.filename()).isEqualTo("Dockerfile");
        assertThat(meta.contentType()).isEqualTo(ContentType.TEXT.value());
        assertThat(meta.strategy()).isEqualTo(com.dockerinit.features.preset.dto.spec.ContentStrategyDTO.EMBEDDED);
        assertThat(meta.sensitive()).isTrue();
        assertThat(meta.etag()).isEqualTo("abcdef");
        assertThat(meta.contentLength()).isEqualTo(123L);
        assertThat(meta.storageProvider()).isEqualTo("s3");
        assertThat(meta.storageKey()).isEqualTo("presets/df--alpine/Dockerfile");
    }

    /* ---------------- helpers ---------------- */

    private static <T> Set<T> setOf(T... items) {
        return new LinkedHashSet<>(Arrays.asList(items));
    }
}