package com.dockerinit.features.preset.repository;

import java.time.Instant;

public interface PresetRepositoryCustom {
    void increaseDownloadCount(String id, long delta);

    void touchUpdatedAt(String id, Instant now, String updateBy);
}
