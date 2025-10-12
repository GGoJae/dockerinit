package com.dockerinit.features.application.presetV2.dockerfile.domain;

import com.dockerinit.features.application.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.application.presetV2.shared.domain.PresetKind;
import com.dockerinit.features.application.presetV2.shared.domain.PresetMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("dockerfile_presets")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "uniq_slug", def = "{'meta.slug': 1}", unique = true),
        @CompoundIndex(name = "find_active_tags_updated",
                def = "{'meta.active': 1, 'meta.tags': 1, 'updatedAt': -1}")
})
public class DockerfilePresetDocument {

    @Id
    private String id;

    private PresetMeta meta;

    private DockerfilePlan plan;

    @Builder.Default
    private PresetKind kind = PresetKind.DOCKERFILE;

    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String updatedBy;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;
}
