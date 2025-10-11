package com.dockerinit.features.application.presetv2.shared.domain;

public enum PresetEventType {
    VIEWED,         //  상세/미리보기 열람
    APPLIED,        // "적용" (폼 채우기)
    COPIED,         // 텍스트 복사(클립보드)
    DOWNLOADED      // zip/파일 다운로드
}
