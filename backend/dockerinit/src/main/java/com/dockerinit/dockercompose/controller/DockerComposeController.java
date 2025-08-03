package com.dockerinit.dockercompose.controller;

import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.dockercompose.dto.DockerComposeRequest;
import com.dockerinit.dockercompose.service.DockerComposeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dockercompose")
@RequiredArgsConstructor
public class DockerComposeController {

    private final DockerComposeService service;

    @Operation(summary = "Docker Compose 프리셋 전체 조회",
            description = "자주 사용하는 Docker Compose 프리셋 목록을 조회합니다.")
    @GetMapping("/presets")
    public ResponseEntity<?> getAllPresets() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllPresets()));
    }


    @Operation(summary = "Docker Compose 프리셋 단건 조회 (문자열)",
            description = "지정한 이름의 Docker Compose 프리셋을 문자열로 반환합니다.")
    @GetMapping("/presets/{name}")
    public ResponseEntity<?> getPreset(@PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.success(service.getPreset(name)));
    }


    @Operation(summary = "Docker Compose 프리셋 단건 다운로드 (ZIP)",
            description = "지정한 이름의 Docker Compose 프리셋을 ZIP 파일로 다운로드합니다.")
    @GetMapping("/presets/{name}/download")
    public ResponseEntity<Resource> downloadPreset(@PathVariable String name) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(service.getPresetAsYml(name));
    }


    @Operation(summary = "사용자 정의 Docker Compose 생성 (문자열)",
            description = "사용자가 입력한 설정에 따라 Docker Compose 파일을 문자열로 생성해 반환합니다.")
    @PostMapping("/generate")
    public ResponseEntity<?> generateCustomCompose(@Valid @RequestBody DockerComposeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.generateCustomComposeYml(request)));
    }


    @Operation(summary = "사용자 정의 Docker Compose 다운로드 (ZIP)",
            description = "사용자가 입력한 설정에 따라 생성된 Docker Compose 파일을 ZIP 파일로 다운로드합니다.")
    @PostMapping("/generate/download")
    public ResponseEntity<Resource> generateCustomComposeDownload(@RequestBody DockerComposeRequest request) {
        Resource zip = service.generateCustomComposeAsZip(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=docker-compose.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
}
