package com.dockerinit.features.application.presetV2.shared.service;

import com.dockerinit.features.application.presetV2.shared.domain.PresetEventType;
import com.dockerinit.features.application.presetV2.shared.domain.PresetKind;
import com.dockerinit.features.application.presetV2.shared.service.spi.PresetMetricPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 프리셋 이벤트(집계) 엔드포인트에서 호출하는 서비스.
 * 실제 증분은 타입별 Port 구현이 수행.
 */
@Slf4j
@Service
public class PresetEventService {

    private final Map<PresetKind, PresetMetricPort> byKind;

    public PresetEventService(List<PresetMetricPort> ports) {
        this.byKind = (ports == null ? List.<PresetMetricPort>of() : ports)
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        PresetMetricPort::kind,
                        Function.identity(),
                        (a, b) -> a));
    }

    public void record(PresetKind kind, String slug, PresetEventType event) {
        PresetMetricPort port = byKind.get(kind);
        if (port == null) {
            log.warn("PresetMetricsPort 가 없습니다. kind = {}, slug = {}, event = {}", kind, slug, event);
            return;
        }
        boolean ok = false;
        try {
            ok = port.increment(slug, event);
        } catch (Exception e) {
            log.error("metrics increment 실패 : kind = {}, slug = {}, event = {}", kind, slug, event, e);
        }
        if (!ok) {
            log.warn("Metrics increment 가 false 를 리턴: kind = {}, slug = {}, event = {}", kind, slug, event);
        }
    }
}
