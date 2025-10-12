package com.dockerinit.features.application.presetV2.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("catalog_versions")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CatalogVersionDocument {
    /** 예: "preset:dockerfile", "preset:compose", "preset:compose_service" */
    @Id
    private String id;

    @Builder.Default
    private Long value = 0L;

    @LastModifiedDate
    private Instant updatedAt;

}
