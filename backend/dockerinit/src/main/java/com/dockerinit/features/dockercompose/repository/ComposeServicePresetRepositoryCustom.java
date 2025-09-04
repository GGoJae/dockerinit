package com.dockerinit.features.dockercompose.repository;

public interface ComposeServicePresetRepositoryCustom {
    void increaseUsedCount(String id, long delta);

    void increaseUsedCountBySlug(String slug, long delta);
}
