package com.dockerinit.features.application.presetv2.shared.api;

import com.dockerinit.features.application.presetv2.shared.domain.PresetEventType;
import com.dockerinit.features.application.presetv2.shared.domain.PresetKind;
import com.dockerinit.features.application.presetv2.shared.service.PresetEventService;
import com.dockerinit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static com.dockerinit.global.constants.HttpInfo.NOSNIFF;
import static com.dockerinit.global.constants.HttpInfo.X_CONTENT_TYPE_OPTIONS;

/**
 * 프론트에서 "복사/적용" 같은 경량 이벤트를 기록할 때 사용하는 엔드포인트.
 * 다운로드 카운트는 서버의 ZIP 다운로드 API에서 자체 집계(별도 호출 불필요).
 */
@RestController
@RequestMapping("/api/presets")
@RequiredArgsConstructor
public class PresetEventsController {

    private final PresetEventService eventService;

    @Operation(summary = "프리셋 이벤트 기록 (복사/적용)")
    @PostMapping("/{kind}/{slug}/events/{event}")
    public ResponseEntity<ApiResponse<Void>> recordEvent(
            @PathVariable String kind,
            @PathVariable String slug,
            @PathVariable String event   // e.g. copied/applied
    ) {
        PresetKind k = PresetKind.fromPath(kind);           // 문자열 → enum 매핑(로워케이스/하이픈 허용 구현 가정)
        PresetEventType e = PresetEventType.fromPath(event);
        eventService.record(k, slug, e);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(0)).cachePrivate().mustRevalidate())
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(null));
    }
}
