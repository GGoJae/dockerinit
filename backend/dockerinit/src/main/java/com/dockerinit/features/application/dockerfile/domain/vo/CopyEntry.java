package com.dockerinit.features.application.dockerfile.domain.vo;

public record CopyEntry(
        String sourceRelPath,  // 상대경로만 (DTO에서 이미 SafeRelPath 검증)
        String targetAbsPath   // 절대경로만
) {}
