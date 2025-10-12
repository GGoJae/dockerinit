package com.dockerinit.features.application.presetV2.shared.domain;

import com.dockerinit.global.exception.IllegalArgumentCustomException;
import com.dockerinit.global.validation.ValidationCollector;

import java.util.Locale;

public enum PresetEventType {
    VIEWED,         //  상세/미리보기 열람
    APPLIED,        // "적용" (폼 채우기)
    COPIED,         // 텍스트 복사(클립보드)
    DOWNLOADED;      // zip/파일 다운로드

    public static PresetEventType fromPath(String raw) {
        ValidationCollector.throwNowIf(raw == null || raw.isBlank(),
                "eventType", "event 가 비어있습니다.", raw);

        String s = raw.trim().toLowerCase(Locale.ROOT).replace('_', '-');

        switch (s) {
            case "view", "viewed", "preview" -> { return VIEWED; }
            case "apply", "applied", "use" -> { return APPLIED; }
            case "copy", "copied" -> { return COPIED; }
            case "download", "downloaded", "dl" -> { return DOWNLOADED; }
        }

        throw new IllegalArgumentCustomException("event 를 알수업습니다.", raw);
    }
}
