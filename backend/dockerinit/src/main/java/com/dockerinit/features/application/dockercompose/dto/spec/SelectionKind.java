package com.dockerinit.features.application.dockercompose.dto.spec;

public enum SelectionKind {
    PRESET,            // 프리셋 그대로
    PRESET_OVERRIDDEN, // 프리셋에서 일부 수정
    CUSTOM             // 완전 커스텀
}
