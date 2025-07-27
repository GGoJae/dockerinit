package com.dockerinit.controller;

import com.dockerinit.dto.apiResponse.ApiResponse;
import com.dockerinit.dto.dockerCompose.DockerComposePreset;
import com.dockerinit.dto.dockerCompose.DockerComposeRequest;
import com.dockerinit.service.DockerComposeService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dockercompose")
@RequiredArgsConstructor
public class DockerComposeController {

    private final DockerComposeService service;

    @GetMapping("/presets")
    public ResponseEntity<?> getAllPresets() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllPresets()));
    }

    @GetMapping("/presets/{name}")
    public ResponseEntity<?> getPreset(@PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.success(service.getPreset(name)));
    }

    @GetMapping("/presets/{name}/download")
    public ResponseEntity<Resource> downloadPreset(@PathVariable String name) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(service.getPresetAsYml(name));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateCustomCompose(@RequestBody DockerComposeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.generateCustomComposeYml(request)));
    }

    @PostMapping("/generate/download")
    public ResponseEntity<Resource> generateCustomComposeDownload(@RequestBody DockerComposeRequest request) {
        Resource zip = service.generateCustomComposeAsZip(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=docker-compose.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
}
