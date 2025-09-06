package com.dockerinit.features.preset.support;

import com.dockerinit.features.preset.dto.spec.PresetKindDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

class PresetSlugFactoryTest {

    private static String prefix(PresetKindDTO kind) {
        // Factory가 kind.code() + "--" 를 prefix로 쓰므로 테스트도 동일 로직으로 기대값 생성
        return kind.code() + "--";
    }

    @Test
    @DisplayName("기본: 길이가 예산 이내면 해시 없이 prefix + tokens + (선택)버전")
    void build_basicWithinBudget_noHash() {
        PresetKindDTO kind = PresetKindDTO.DOCKERFILE;
        String slug = PresetSlugFactory.build(kind, List.of("java", "spring-boot"), 1);

        assertThat(slug).startsWith(prefix(kind) + "java-spring-boot");
        assertThat(slug).endsWith("-v1");
        assertThat(slug.length()).isLessThanOrEqualTo(80);
        // 해시(10자리 hex)가 붙지 않았는지 확인
        assertThat(slug).doesNotMatch(".*-[0-9a-f]{10}-v1$");
    }

    @Test
    @DisplayName("정규화: 공백/언더스코어 → 하이픈, 소문자 변환, 연속 하이픈 정리")
    void build_normalization() {
        PresetKindDTO kind = PresetKindDTO.COMPOSE;
        String slug = PresetSlugFactory.build(kind, List.of(" My   App ", "SPRING__BOOT"), 2);

        assertThat(slug)
                .startsWith(prefix(kind) + "my-app-spring-boot")
                .endsWith("-v2");
        assertThat(slug).doesNotContain("  ").doesNotContain("__").doesNotContain("SPRING");
    }

    @Test
    @DisplayName("null/blank 토큰은 무시된다")
    void build_ignoresNullAndBlankTokens() {
        PresetKindDTO kind = PresetKindDTO.BUNDLE;
        String slug = PresetSlugFactory.build(kind, Arrays.asList(null, "  ", "node"), null);

        assertThat(slug).isEqualTo(prefix(kind) + "node");
    }

    @Test
    @DisplayName("baseTokens 가 null 이여도 빈리스트로 처리한다")
    void build_ignoresTokensIsNull() {
        PresetKindDTO kind = PresetKindDTO.BUNDLE;
        String slug = PresetSlugFactory.build(kind, null, null);

        assertThat(slug).isEqualTo(prefix(kind));
    }

    @Test
    @DisplayName("긴 토큰은 (trunk)-(10자리hex) 형태로 해시가 붙고 전체 길이는 80 이하")
    void build_longTokens_triggersHash() {
        PresetKindDTO kind = PresetKindDTO.DOCKERFILE;
        String veryLong = "a".repeat(200); // 예산 초과를 유도
        String slug = PresetSlugFactory.build(kind, List.of("java", veryLong, "alpine"), 9);

        // 패턴: <prefix><trunk>-<10hex>-v9
        String re = "^" + Pattern.quote(prefix(kind)) + "[a-z0-9-]+-[0-9a-f]{10}-v9$";
        assertThat(slug).matches(re);
        assertThat(slug.length()).isLessThanOrEqualTo(80);
    }

    @Test
    @DisplayName("버전이 null이면 버전 접미사가 없다")
    void build_noVersionSuffixWhenNull() {
        PresetKindDTO kind = PresetKindDTO.COMPOSE;
        String slug = PresetSlugFactory.build(kind, List.of("python", "fastapi"), null);

        assertThat(slug).isEqualTo(prefix(kind) + "python-fastapi");
        assertThat(slug).doesNotContain("-v");
    }

    @Test
    @DisplayName("숫자 접미사 도우미: base + \"-n\"")
    void withNumericSuffix_appendsNumber() {
        String base = "df--java-spring";
        String s = PresetSlugFactory.withNumericSuffix(base, 3);
        assertThat(s).isEqualTo("df--java-spring-3");
    }
}
