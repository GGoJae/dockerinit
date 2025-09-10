package com.dockerinit.features.application.preset.api;

import com.dockerinit.features.application.preset.dto.response.PresetArtifactResponse;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.PackageResult;
import com.dockerinit.features.application.preset.dto.spec.PresetArtifactMetaDTO;
import com.dockerinit.features.application.preset.dto.response.PresetDetailResponse;
import com.dockerinit.features.application.preset.dto.response.PresetSummaryResponse;
import com.dockerinit.features.application.preset.dto.spec.PresetKindDTO;
import com.dockerinit.features.application.preset.service.PresetService;
import com.dockerinit.features.support.CatalogVersionService;
import com.dockerinit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dockerinit.global.constants.HttpInfo.*;

@RestController
@RequestMapping("/api/presets")
@RequiredArgsConstructor
public class PresetController {

    private final PresetService service;
    private final CatalogVersionService catalogVersionService;

    @Operation(summary = "프리셋 리스트 목록",
    description = "조건에 맞는 프리셋 목록을 받습니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PresetSummaryResponse>>> list(
            @RequestParam(required = false) PresetKindDTO kind,
            @RequestParam(required = false)Set<String> tags,
            Pageable pageable,
            WebRequest request
            ) {

        String normKind = (kind == null) ? "-" : kind.toString();
        String normTags = (tags == null || tags.isEmpty())
                ? "-"
                : tags.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.joining(","));

        String ver = catalogVersionService.getComposePresetCatalogVer();
        String etag = "\"" + String.join("|",
                "preset:list", normKind, normTags,
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

        Page < PresetSummaryResponse > page = service.list(kind, tags, pageable);

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePrivate())
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(page));
    }


    @Operation(summary = "프리셋 단건 조회",
            description = "단건의 프리셋의 디테일을 받습니다.")
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PresetDetailResponse>> get(
            @PathVariable String rawSlug,
            WebRequest request
    ) {

        PresetDetailResponse dto = service.get(rawSlug);

        String etag = "\"" + dto.updatedAt().toEpochMilli() + "\"";

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
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


    @Operation(summary = "특정 프리셋 아티팩트 목록",
    description = "특정 프리셋이 가지고 있는 아티팩트의 목록을 가져옵니다.")
    @GetMapping("/{slug}/artifacts")
    public ResponseEntity<ApiResponse<PresetArtifactResponse>> artifacts(
            @PathVariable String rawSlug,
            WebRequest request
    ) {

        PresetArtifactResponse res = service.artifacts(rawSlug);
        long last = res.updatedAt().toEpochMilli();
        String etag = "\"" + last + "\"";

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .lastModified(last)
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePrivate())
                    .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .lastModified(last)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePrivate())
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(res));
    }

    @Operation(summary = "프리셋 패키지.zip 다운로드",
    description = "사용자가 선택한 프리셋과 부속 파일들을 zip으로 받습니다.")
    @PostMapping(value = "/{slug}/package", produces = APPLICATION_ZIP_VALUE)
    public ResponseEntity<Resource> downloadZip(
            @PathVariable String slug,
            @RequestParam(required = false) Set<FileType> targets,
            WebRequest req
    ) {
        PackageResult pkg = service.packagePreset(slug, targets);

        if (pkg.getEtag() != null && req.checkNotModified(pkg.getEtag())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(pkg.getEtag())
                    .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        ContentDisposition cd = ContentDisposition.attachment().filename(pkg.getFilename(), StandardCharsets.UTF_8).build();
        HttpHeaders headers = new HttpHeaders();
        if (pkg.getEtag() != null) headers.setETag(pkg.getEtag());
        headers.setContentType(MediaType.parseMediaType(pkg.getContentTypeValue()));
        headers.setContentDisposition(cd);
        headers.set(X_CONTENT_TYPE_OPTIONS, NOSNIFF);
        headers.set(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION);

        if (pkg.isSensitive()) {
            headers.setCacheControl(NO_STORE);
            headers.setPragma(NO_CACHE);
            headers.set(HttpHeaders.EXPIRES, "0");
        } else {
            headers.setCacheControl(CacheControl.maxAge(Duration.ofMinutes(60)).cachePrivate());
        }
        pkg.contentLength().ifPresent(headers::setContentLength);

        Resource body = pkg.fold(
                ByteArrayResource::new,
                s ->  new InputStreamResource(s.get())
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }
}
