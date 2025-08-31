package com.dockerinit.features.preset.domain;

import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.model.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class PresetArtifact {
    private FileType fileType;
    private String filename;
    private ContentType contentType;

    private ContentStrategy strategy;
    private String embeddedContent;
    private String storageProvider;         // 추후 s3 나 Object_storage 확장용
    private String storageKey;              // 추후 s3 나 Object_storage 확장용
    private String etag;
    private Long contentLength;
}
