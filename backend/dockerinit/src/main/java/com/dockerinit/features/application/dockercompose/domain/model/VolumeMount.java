package com.dockerinit.features.application.dockercompose.domain.model;

import com.dockerinit.global.validation.ValidationCollector;

public record VolumeMount(
        String source,
        String target,
        boolean readOnly,
        Type type
) {
    public enum Type {BIND, VOLUME, TMPFS}
    public VolumeMount {
        ValidationCollector.create()
                .requiredTrue(target != null && target.startsWith("/"), "target", "target은 절대경로여야 합니다.", target)
                    .notBlank("source", source,"source 는 필수입니다.")
                    .notNull("type", type, "type은 null 이 허용되지 않습니다.")
                    .throwIfInvalid();
    }

}
