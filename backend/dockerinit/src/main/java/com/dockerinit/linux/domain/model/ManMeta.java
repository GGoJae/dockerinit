package com.dockerinit.linux.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ManMeta {
    private final String section;
    private final String distro;
    private final String source;
    private final String sourceHash;
    private final Instant importedAt;

    private ManMeta(String section, String distro, String source, String sourceHash, Instant importedAt) {
        this.section = section;
        this.distro = distro;
        this.source = source;
        this.sourceHash = sourceHash;
        this.importedAt = importedAt;
    }

    public static ManMeta of(String section, String distro, String source, String sourceHash, Instant importedAt) {
        return new ManMeta(section, distro, source, sourceHash, importedAt);
    }
}
