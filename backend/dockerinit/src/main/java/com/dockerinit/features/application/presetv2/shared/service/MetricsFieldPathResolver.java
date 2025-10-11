package com.dockerinit.features.application.presetv2.shared.service;

import com.dockerinit.features.application.presetv2.shared.domain.PresetEventType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetricsFieldPathResolver {

    public static String path(PresetEventType e) {
        return switch (e) {
            case VIEWED     -> "meta.metrics.viewed";
            case APPLIED    -> "meta.metrics.applied";
            case COPIED     -> "meta.metrics.copied";
            case DOWNLOADED -> "meta.metrics.downloaded";
        };
    }
}
