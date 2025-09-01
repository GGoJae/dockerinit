package com.dockerinit.features.preset.api;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.PackageResult;
import com.dockerinit.features.preset.dto.response.PresetArtifactMetaResponse;
import com.dockerinit.features.preset.dto.response.PresetSummaryResponse;
import com.dockerinit.features.preset.dto.spec.PresetKindDTO;
import com.dockerinit.features.preset.service.PresetService;
import com.dockerinit.global.response.ApiResponse;
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
import java.util.List;
import java.util.Set;

import static com.dockerinit.global.constants.HttpInfo.*;

@RestController
@RequestMapping("/api/presets")
@RequiredArgsConstructor
public class PresetController {

    private final PresetService service;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PresetSummaryResponse>>> list(
            @RequestParam(required = false) PresetKindDTO kind,
            @RequestParam(required = false)Set<String> tags,
            Pageable pageable
            ) {
        return ResponseEntity.ok(
                ApiResponse.success(service.list(kind, tags, pageable))
        );
    }

    @GetMapping("/{slug}/artifacts")
    public ResponseEntity<ApiResponse<List<PresetArtifactMetaResponse>>> artifacts(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(service.artifacts(slug))
        );
    }

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
                    .header((X_CONTENT_TYPE_OPTIONS), NOSNIFF)
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
            headers.setCacheControl("private, max-age=3600");
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
