package com.dockerinit.features.dockerfile.api;

import com.dockerinit.features.dockerfile.dto.request.DockerfilePresetRequest;
import com.dockerinit.features.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.dockerfile.dto.response.DockerfileResponse;
import com.dockerinit.features.model.PackageResult;
import com.dockerinit.features.dockerfile.service.DockerfileService;
import com.dockerinit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.dockerinit.global.constants.HttpInfo.*;

@RestController
@RequestMapping("/api/dockerfile")
@RequiredArgsConstructor
public class DockerfileController {

    private final DockerfileService service;

    @Operation(summary = "Dockerfile 생성",
            description = "요청 형식에 따라 Dockerfile 내용을 문자열로 생성해 반환합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<DockerfileResponse>> render(@Valid @RequestBody DockerfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.renderContent(request)));
    }


    @Operation(summary = "Dockerfile 프리셋 전체 조회",
            description = "자주 사용하는 Dockerfile 프리셋 전체 목록을 제공합니다.")
    @GetMapping("/presets")
    public ResponseEntity<ApiResponse<List<DockerfilePresetRequest>>> listPresets() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllPresets()));
    }


    @Operation(summary = "Dockerfile 프리셋 단건 조회",
            description = "지정한 이름의 Dockerfile 프리셋을 반환합니다.")
    @GetMapping("/presets/{name}")
    public ResponseEntity<ApiResponse<DockerfilePresetRequest>> getPreset(@PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.success(service.getPreset(name)));
    }


    @Operation(summary = "Dockerfile, env 등 패키지 다운로드 (ZIP)",
            description = "요청 형식에 따라 생성된 패키지를 ZIP 파일 형태로 다운로드합니다.")
    @PostMapping(value = "/package", produces = APPLICATION_ZIP_VALUE)
    public ResponseEntity<Resource> downloadAsZip(@Valid @RequestBody DockerfileRequest request, WebRequest webRequest) {
        PackageResult pkg = service.downloadPackageAsZip(request);

        if (pkg.getEtag() != null && webRequest.checkNotModified(pkg.getEtag())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(pkg.getEtag())
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        ContentDisposition cd = ContentDisposition.attachment().filename(pkg.getFilename(), StandardCharsets.UTF_8)
                .build();

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
        pkg.contentLength().ifPresent(cl -> headers.setContentLength(cl));

        Resource body = pkg.fold(
                ByteArrayResource::new,
                s ->  new InputStreamResource(s.get())
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }


}
