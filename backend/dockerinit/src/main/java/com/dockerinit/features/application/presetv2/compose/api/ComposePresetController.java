package com.dockerinit.features.application.presetv2.compose.api;

import com.dockerinit.features.application.presetv2.shared.dto.response.PresetDetailResponse;
import com.dockerinit.features.application.presetv2.shared.dto.response.PresetSummaryResponse;
import com.dockerinit.features.application.presetv2.shared.service.CatalogVersionService;
import com.dockerinit.features.application.presetv2.shared.service.PresetQueryService;
import com.dockerinit.features.application.presetv2.shared.domain.PresetKind;
import com.dockerinit.features.application.presetv2.shared.support.ETagUtil;
import com.dockerinit.features.application.preset.compose.materializer.ComposePresetMaterializer;
import com.dockerinit.features.application.preset.compose.renderer.ComposeRenderer;
import com.dockerinit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.dockerinit.global.constants.HttpInfo.NOSNIFF;
import static com.dockerinit.global.constants.HttpInfo.X_CONTENT_TYPE_OPTIONS;

@RestController
@RequestMapping("/api/presets/composes")
@RequiredArgsConstructor
public class ComposePresetController {

    private final PresetQueryService queryService;
    private final CatalogVersionService catalogVersionService;
    private final ComposePresetMaterializer materializer;
    private final ComposeRenderer renderer;

    @Operation(summary = "Compose 프리셋 목록")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PresetSummaryResponse>>> list(
            @RequestParam(required = false) Set<String> tags,
            Pageable pageable,
            WebRequest request
    ) {
        String normTags = normalizeTags(tags);
        long ver = catalogVersionService.get(PresetKind.COMPOSE);
        String etag = ETagUtil.strong("compose:list", normTags,
                "p=" + pageable.getPageNumber(),
                "s=" + pageable.getPageSize(),
                "sort=" + pageable.getSort(),
                "ver=" + ver);

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePrivate())
                    .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        Page<PresetSummaryResponse> page = queryService.list(PresetKind.COMPOSE, tags, pageable);
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePrivate())
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(page));
    }

    @Operation(summary = "Compose 프리셋 상세")
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PresetDetailResponse>> get(
            @PathVariable String slug,
            WebRequest request
    ) {
        PresetDetailResponse dto = queryService.get(PresetKind.COMPOSE, slug);
        String etag = ETagUtil.strong("compose:detail", slug,
                "upd=" + dto.updatedAt().toEpochMilli(),
                "v=" + dto.version());

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .lastModified(dto.updatedAt().toEpochMilli())
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePrivate())
                    .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .lastModified(dto.updatedAt().toEpochMilli())
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePrivate())
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(dto));
    }

    @Operation(summary = "docker-compose.yml 렌더(미리보기)")
    @GetMapping(value = "/{slug}/render", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<byte[]> render(
            @PathVariable String slug,
            WebRequest request
    ) {
        PresetDetailResponse dto = queryService.get(PresetKind.COMPOSE, slug);
        String etag = ETagUtil.strong("compose:render", slug,
                "upd=" + dto.updatedAt().toEpochMilli(),
                "v=" + dto.version());

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .lastModified(dto.updatedAt().toEpochMilli())
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePrivate())
                    .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        var plan = materializer.toPlan(slug);
        String out = renderer.render(plan);

        return ResponseEntity.ok()
                .eTag(etag)
                .lastModified(dto.updatedAt().toEpochMilli())
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePrivate())
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .contentType(MediaType.TEXT_PLAIN)
                .body(out.getBytes(StandardCharsets.UTF_8));
    }

    private static String normalizeTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) return "-";
        SortedSet<String> set = new TreeSet<>();
        for (String t : tags) if (t != null && !t.isBlank()) set.add(t.trim().toLowerCase(Locale.ROOT));
        return String.join(",", set);
    }
}
