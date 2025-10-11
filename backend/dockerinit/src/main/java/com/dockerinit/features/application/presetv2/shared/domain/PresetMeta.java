package com.dockerinit.features.application.presetv2.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.Set;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PresetMeta {

    @Indexed(unique = true)
    private String slug;

    private String displayName;
    private String description;

    @Builder.Default
    private Set<String> tags = Set.of();

    private Integer schemaVersion;
    private RenderPolicy renderPolicy;
    private String instructions;

    private Boolean active;
    private Boolean deprecated;
    private String deprecationNote;

    @Builder.Default
    private PresetMetrics metrics = new PresetMetrics();

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
