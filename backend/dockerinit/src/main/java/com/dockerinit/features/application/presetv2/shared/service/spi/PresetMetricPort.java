package com.dockerinit.features.application.presetv2.shared.service.spi;

import com.dockerinit.features.application.presetv2.shared.domain.PresetEventType;
import com.dockerinit.features.application.presetv2.shared.domain.PresetKind;

/**
 * 타입별(도커파일/컴포즈/서비스조각) 프리셋 문서의 metrics 증가를 수행하는 SPI.
 * 각 타입 모듈에서 구현체를 제공한다.
 */
public interface PresetMetricPort {
   PresetKind kind();
    /** slug 기준 문서를 찾아 해당 이벤트 카운터 +1. 성공시 true */
    boolean increment(String slug, PresetEventType event);
}
