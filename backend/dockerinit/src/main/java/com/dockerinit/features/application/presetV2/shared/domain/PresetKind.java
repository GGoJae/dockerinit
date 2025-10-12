package com.dockerinit.features.application.presetV2.shared.domain;

import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.validation.ValidationCollector;

import java.util.Locale;

public enum PresetKind {
    DOCKERFILE,
    COMPOSE,
    COMPOSE_SERVICE;

    public static PresetKind fromPath(String raw) {
        ValidationCollector.throwNowIf(raw == null || raw.isBlank(),
                "kind",
                "kind 가 비어있습니다.",
                raw);

        String s = raw.trim().toLowerCase(Locale.ROOT).replace("_", "-");


        switch (s) {
            case "dockerfile", "dockerfiles", "df" -> { return DOCKERFILE; }
            case "compose", "docker-compose", "docker-composes", "compose-full" -> { return COMPOSE; }
            case "compose-service", "compose-services", "csvc", "service", "services" -> { return COMPOSE_SERVICE; }
        }

        throw new InvalidInputCustomException("kind 를 알수없습니다.", raw);
    }

}
