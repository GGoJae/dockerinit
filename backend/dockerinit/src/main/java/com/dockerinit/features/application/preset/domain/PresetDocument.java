package com.dockerinit.features.application.preset.domain;

import com.dockerinit.features.model.FileType;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Document("presets")
@Getter @Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PresetDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;
    private String displayName;
    private String description;

    @Indexed
    private PresetKind presetKind;

    @Builder.Default
    private Set<String> tags = Set.of();
    private Integer schemaVersion;
    private RenderPolicy renderPolicy;

    @Builder.Default
    private List<PresetArtifact> artifacts = List.of();

    @Builder.Default
    private Set<FileType> defaultTargets = Set.of();
    private String instructions;

    private Boolean active;
    private Boolean deprecated;
    private String deprecationNote;

    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String updatedBy;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    private Long downloadCount;
    @Version
    private Long version;
}
