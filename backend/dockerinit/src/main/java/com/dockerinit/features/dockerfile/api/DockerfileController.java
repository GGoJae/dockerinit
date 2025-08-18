package com.dockerinit.features.dockerfile.api;

import com.dockerinit.features.dockerfile.dto.DockerfilePreset;
import com.dockerinit.features.support.FileResult;
import com.dockerinit.features.dockerfile.dto.DockerfileRequest;
import com.dockerinit.features.dockerfile.dto.DockerfileResponse;
import com.dockerinit.features.dockerfile.service.DockerfileService;
import com.dockerinit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
    public ResponseEntity<ApiResponse<List<DockerfilePreset>>> listPresets() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllPresets()));
    }


    @Operation(summary = "Dockerfile 프리셋 단건 조회",
            description = "지정한 이름의 Dockerfile 프리셋을 반환합니다.")
    @GetMapping("/presets/{name}")
    public ResponseEntity<ApiResponse<DockerfilePreset>> getPreset(@PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.success(service.getPreset(name)));
    }


    @Operation(summary = "Dockerfile 다운로드 (ZIP)",
            description = "요청 형식에 따라 생성된 Dockerfile을 ZIP 파일 형태로 다운로드합니다.")
    @PostMapping(value = "/download", produces = APPLICATION_ZIP_VALUE)
    public ResponseEntity<Resource> downloadAsZip(@RequestBody DockerfileRequest request) {
        FileResult fileResult = service.buildZip(request);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileResult.filename(), StandardCharsets.UTF_8)
                        .build()
        );
        headers.set(X_CONTENT_TYPE_OPTIONS, NOSNIFF);
        headers.set(HttpHeaders.PRAGMA, NO_CACHE);
        headers.set(HttpHeaders.EXPIRES, "0");
        headers.set(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(fileResult.contentType())
                .contentLength(fileResult.contentLength())
                .body(fileResult.resource());
    }


}
