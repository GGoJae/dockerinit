package com.dockerinit.features.application.presetV2.dockerfile.api;

import com.dockerinit.features.application.dockerfile.dto.response.DockerfilePlanResponse;
import com.dockerinit.features.application.presetV2.dockerfile.dto.DockerfilePlanProjection;
import com.dockerinit.features.application.presetV2.dockerfile.renderer.DockerfileRendererV2;
import com.dockerinit.features.application.presetV2.shared.domain.PresetKind;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetDetailResponseV2;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetSuggestDTO;
import com.dockerinit.features.application.presetV2.shared.dto.response.PresetSummaryResponseV2;
import com.dockerinit.features.application.presetV2.shared.service.CatalogVersionServiceV2;
import com.dockerinit.features.application.presetV2.shared.service.PresetQueryService;
import com.dockerinit.features.application.presetV2.shared.support.ETagUtil;
import com.dockerinit.global.constants.Version;
import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.global.support.hash.Hashes;
import com.dockerinit.global.support.json.CanonicalJson;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static com.dockerinit.global.constants.HttpInfo.NOSNIFF;
import static com.dockerinit.global.constants.HttpInfo.X_CONTENT_TYPE_OPTIONS;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;

@RestController
@RequestMapping("/api/presets-v2/dockerfiles")
@RequiredArgsConstructor
public class DockerfilePresetControllerV2 {

    private final PresetQueryService queryService;                 // 공유 쿼리 서비스(Kind 인자)
    private final CatalogVersionServiceV2 catalogVersionService;     // 카탈로그 버전
    private final DockerfileRendererV2 renderer;                     // 도커파일 렌더러

    @Operation(summary = "Dockerfile 프리셋 목록")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PresetSummaryResponseV2>>> list(
            @RequestParam(required = false) Set<String> tags,
            Pageable pageable,
            WebRequest request
    ) {
        String normTags = normalizeTags(tags);
        long ver = catalogVersionService.get(PresetKind.DOCKERFILE);
        String etag = ETagUtil.strong("df:list", normTags,
                "p=" + pageable.getPageNumber(),
                "s=" + pageable.getPageSize(),
                "sort=" + pageable.getSort(),
                "v=" + ver);

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(NOT_MODIFIED)
                    .eTag(etag)
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic())
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        var page = queryService.summaryList(PresetKind.DOCKERFILE, tags, pageable);
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic())
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(page));
    }

    @Operation(summary = "Dockerfile 프리셋 slug, displayName 목록")
    @GetMapping("/suggest")
    public ResponseEntity<ApiResponse<List<PresetSuggestDTO>>> suggest(WebRequest request) {
        long ver = catalogVersionService.get(PresetKind.DOCKERFILE);
        String etag = ETagUtil.strong("df:sug", "v=" + ver);

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(NOT_MODIFIED)
                    .eTag(etag)
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        var suggests = queryService.suggest(PresetKind.DOCKERFILE);

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(suggests));

    }

    @Operation(summary = "Dockerfile 프리셋 상세")
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PresetDetailResponseV2>> get(
            @PathVariable String slug,
            WebRequest request
    ) {
        PresetDetailResponseV2 dto = queryService.getDetail(PresetKind.DOCKERFILE, slug);
        String etag = ETagUtil.strong("df:detail", slug,
                "upd=" + dto.updatedAt().toEpochMilli(),
                "v=" + dto.version());

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(NOT_MODIFIED)
                    .eTag(etag)
                    .lastModified(dto.updatedAt().toEpochMilli())
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(30)).cachePublic())
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .lastModified(dto.updatedAt().toEpochMilli())
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(30)).cachePublic())
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(dto));
    }

    @Operation(summary = "프리셋의 Dockerfile Plan 불러오기")
    @GetMapping("/{slug}/plan")
    public ResponseEntity<ApiResponse<DockerfilePlanResponse>> getPlan(
            @PathVariable String slug,
            WebRequest request
    ) {
        /*
        나중에 planHash 값을 저장하는 document 만들면
        그떄 캐시 적용, 지금은 이미 db에서 plan 값을 가져왔으므로 etag 비교 무의미 오히려
        etag 값 계산하는 로직이 리소스 더 든다.
         */

        var plan = queryService.getPlanResponse(slug);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                .body(ApiResponse.success(plan));

    }


    @Operation(summary = "Dockerfile 텍스트 렌더(미리보기)")
    @GetMapping(value = "/{slug}/render", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<byte[]> render(
            @PathVariable String slug,
            WebRequest request
    ) {
        DockerfilePlanProjection planAndInfo = queryService.getPlanAndInfo(slug);
        String planHash = Hashes.sha256(CanonicalJson.of(planAndInfo.getPlan()));
        String etag = ETagUtil.strong("df:render", slug,
                "dphs=" + planHash,
                "dfrv=" + Version.DOCKERFILE_RENDERER_VERSION);

        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(NOT_MODIFIED)
                    .eTag(etag)
                    .lastModified(planAndInfo.getUpdatedAt().toEpochMilli())
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(30)).cachePublic())
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }


        var out = renderer.render(planAndInfo.getPlan());

        return ResponseEntity.ok()
                .eTag(etag)
                .lastModified(planAndInfo.getUpdatedAt().toEpochMilli())
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(30)).cachePublic())
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
