package com.dockerinit.features.dockerfile.controller;

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

@RestController
@RequestMapping("/api/dockerfile")
@RequiredArgsConstructor
public class DockerfileController {

    private final DockerfileService service;

    @Operation(summary = "Dockerfile 생성",
            description = "요청 형식에 따라 Dockerfile 내용을 문자열로 생성해 반환합니다.")
    @PostMapping
    public ResponseEntity<?> generateDockerfile(@Valid @RequestBody DockerfileRequest request) {
        String content = service.generateDockerfile(request);
        return ResponseEntity.ok(ApiResponse.success(new DockerfileResponse(content)));
    }


    @Operation(summary = "Dockerfile 프리셋 전체 조회",
            description = "자주 사용하는 Dockerfile 프리셋 전체 목록을 제공합니다.")
    @GetMapping("/presets")
    public ResponseEntity<?> getPresets() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllPresets()));
    }


    @Operation(summary = "Dockerfile 프리셋 단건 조회",
            description = "지정한 이름의 Dockerfile 프리셋을 반환합니다.")
    @GetMapping("/presets/{name}")
    public ResponseEntity<?> getPresentByName(@PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.success(service.getPresentByName(name)));
    }


    @Operation(summary = "Dockerfile 다운로드 (ZIP)",
            description = "요청 형식에 따라 생성된 Dockerfile을 ZIP 파일 형태로 다운로드합니다.")
    @PostMapping(value = "/download", produces = "application/zip")
    public ResponseEntity<Resource> downloadDockerfileZip(@RequestBody DockerfileRequest request) {
        FileResult fileResult = service.downloadDockerfile(request);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileResult.filename(), StandardCharsets.UTF_8)
                        .build()
        );
        headers.set("X-Content-type-Options", "nosniff");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(fileResult.contentType())
                .contentLength(fileResult.contentLength())
                .body(fileResult.resource());
    }


}
