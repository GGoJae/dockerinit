package com.dockerinit.features.dockercompose.api;

import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.dockercompose.dto.response.ComposeAsStringResponse;
import com.dockerinit.features.dockercompose.dto.response.ComposePresetResponse;
import com.dockerinit.features.model.PackageResult;
import com.dockerinit.features.support.FileResult;
import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.features.dockercompose.service.DockerComposeService;
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
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static com.dockerinit.global.constants.HttpInfo.*;

@RestController
@RequestMapping("/api/dockercompose")
@RequiredArgsConstructor
public class DockerComposeController {

    private final DockerComposeService service;

    @Operation(summary = "Docker Compose 프리셋 전체 조회",
            description = "자주 사용하는 Docker Compose 프리셋 목록을 조회합니다.")
    @GetMapping("/presets")
    public ResponseEntity<ApiResponse<List<ComposePresetResponse>>> getAllPresets() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllPresets()));
    }


    @Operation(summary = "Docker Compose 프리셋 단건 조회 (문자열)",
            description = "지정한 이름의 Docker Compose 프리셋을 문자열로 반환합니다.")
    @GetMapping("/presets/{name}")
    public ResponseEntity<ApiResponse<ComposePresetResponse>> getPreset(@PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.success(service.getPreset(name)));
    }


    @Operation(summary = "Docker Compose 프리셋 단건 다운로드 (ZIP)",
            description = "지정한 이름의 Docker Compose 프리셋을 ZIP 파일로 다운로드합니다.")
    @GetMapping(value = "/presets/{name}/download", produces = APPLICATION_ZIP_VALUE)
    public ResponseEntity<Resource> downloadPreset(@PathVariable String name, WebRequest request) {
        FileResult file = service.getPresetAsZip(name);

        CacheControl cache = CacheControl.maxAge(Duration.ofDays(1))
                .cachePublic()
                .mustRevalidate();

        if (Objects.nonNull(file.eTag()) && request.checkNotModified(file.eTag())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(file.eTag())
                    .cacheControl(cache)
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(file.filename(), StandardCharsets.UTF_8)
                        .build()
        );
        headers.set(X_CONTENT_TYPE_OPTIONS, NOSNIFF);

        return ResponseEntity.ok()
                .eTag(file.eTag())
                .cacheControl(cache)
                .headers(headers)
                .contentType(file.contentType())
                .contentLength(file.contentLength())
                .body(file.resource());
    }


    @Operation(summary = "사용자 정의 Docker Compose 생성 (문자열)",
            description = "사용자가 입력한 설정에 따라 Docker Compose 파일을 문자열로 생성해 반환합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ComposeAsStringResponse>> generateCustomCompose(@Valid @RequestBody ComposeRequestV1 request) {
        return ResponseEntity.ok(ApiResponse.success(service.renderComposeYaml(request)));
    }


    @Operation(summary = "사용자 정의 Docker Compose package 다운로드 (ZIP)",
            description = "사용자가 입력한 설정에 따라 생성된 Docker Compose 파일을 ZIP 파일로 다운로드합니다.")
    @PostMapping("/package")
    public ResponseEntity<Resource> generateCustomComposeDownload(@RequestBody ComposeRequestV1 request, WebRequest webRequest) {
        PackageResult pkg = service.downloadComposePackage(request);


        if (Objects.nonNull(pkg.getEtag()) && webRequest.checkNotModified(pkg.getEtag())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(pkg.getEtag())
                    .header(X_CONTENT_TYPE_OPTIONS, NOSNIFF)
                    .build();
        }

        ContentDisposition cd = ContentDisposition.attachment().filename(pkg.getFilename(), StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        if (Objects.nonNull(pkg.getEtag())) headers.setETag(pkg.getEtag());
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
