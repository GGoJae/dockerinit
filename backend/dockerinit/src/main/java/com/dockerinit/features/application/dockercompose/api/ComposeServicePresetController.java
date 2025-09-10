package com.dockerinit.features.application.dockercompose.api;

import com.dockerinit.features.application.dockercompose.dto.response.ComposeServicePresetDetailResponse;
import com.dockerinit.features.application.dockercompose.dto.response.ComposeServicePresetSummaryResponse;
import com.dockerinit.features.application.dockercompose.dto.spec.CategoryDTO;
import com.dockerinit.features.application.dockercompose.service.ComposeServicePresetService;
import com.dockerinit.features.support.CatalogVersionService;
import com.dockerinit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dockerinit.global.constants.HttpInfo.NOSNIFF;
import static com.dockerinit.global.constants.HttpInfo.X_CONTENT_TYPE_OPTIONS;

@RestController
@RequestMapping("/api/compose/service-presets")
@RequiredArgsConstructor
public class ComposeServicePresetController {

    private final ComposeServicePresetService service;
    private final CatalogVersionService catalogVersionService;


    @Operation(summary = "요청 컴포즈의 서비스에 대한 프리셋 목록 제공",
            description = "요청한 카테고리와 태그에 해당하는 프리셋을 리스트로 제공합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ComposeServicePresetSummaryResponse>>> list(
            @RequestParam(required = false) CategoryDTO category,
            @RequestParam(required = false) Set<String> tags,
            Pageable pageable,
            WebRequest request
    ) {
        String normKind = (category == null) ? "-" : category.name();
        String normTags = (tags == null || tags.isEmpty())
                ? "-"
                : tags.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.joining(","));

        String ver = catalogVersionService.getPresetCatalogVer();
        String etag = "\"" + String.join("|",
                "compose:preset:list", normKind, normTags,
                String.valueOf(pageable.getPageNumber()),
                String.valueOf(pageable.getPageSize()),
                String.valueOf(pageable.getSort()),
                ver
        ) + "\"";

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePrivate())
                    .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        Page<ComposeServicePresetSummaryResponse> page = service.list(category, tags, pageable);

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePrivate())
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(page));
    }


    @Operation(summary = "컴포즈 서비스 프리셋 세부사항 요청",
    description = "요청한 컴포즈 서비스 프리셋에 대한 디테일을 보여줍니다.")
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ComposeServicePresetDetailResponse>> get(
            @PathVariable String slug, WebRequest request
    ) {
        ComposeServicePresetDetailResponse res = service.get(slug);
        Instant last = Optional.ofNullable(res.updatedAt()).orElseGet(res::createdAt);

        String etag = "W/\"" + slug + ":" + res.updatedAt().toEpochMilli() + "\"";
        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .lastModified(last.toEpochMilli())
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                    .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .lastModified(last.toEpochMilli())
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(res));
    }
}
