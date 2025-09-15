package com.dockerinit.features.application.dockercompose.domain.composePreset;

import com.dockerinit.features.application.dockercompose.domain.model.Service;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@CompoundIndexes({
        @CompoundIndex(name = "active_category_tags_idx",
                def = "{'active':1,'category':1,'tags':1}"),
        @CompoundIndex(name = "active_updated_idx",
                def = "{'active':1,'updatedAt':-1}")
})
@Document("compose_service_presets")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComposeServicePresetDocument {
    // TODO 빌더 -> 업데이터로 업데이트 불가한 항목 제거하고 불변으로 만들기

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;                 // ex) "lang-java", "db-mysql"

    private String displayName;          // ex) "Java (Spring Boot)"
    private String description;

    @Indexed
    private Category category;

    @Builder.Default
    @Indexed
    private Set<String> tags = Set.of(); // 네임스페이스 태그: lang:java, framework:spring-boot, db:mysql ...

    private Integer schemaVersion;

    /** 핵심: 서비스 조각(구조체) */
    private Service service;

    /** 폼 기본값/힌트 (옵션) */
    @Builder.Default
    private Map<String, String> suggestedEnvDefaults = Map.of();

    private Boolean active;

    @CreatedBy  private String createdBy;
    @LastModifiedBy private String updatedBy;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    @Version private Long version;

    /** 선택/조립 집계 */
    private Long usedCount;
}
