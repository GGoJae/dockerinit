package com.dockerinit.dockerfile.controller;

import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.dockerfile.dto.DockerfileRequest;
import com.dockerinit.dockerfile.dto.DockerfileResponse;
import com.dockerinit.dockerfile.service.DockerfileService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadDockerfileZip(@RequestBody DockerfileRequest request) {
        byte[] zipBytes = service.downloadDockerfile(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename("dockerfile-template.zip").build());

        return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
    }


}
