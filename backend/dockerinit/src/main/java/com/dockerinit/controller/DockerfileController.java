package com.dockerinit.controller;

import com.dockerinit.dto.makeDockerfile.DockerfilePreset;
import com.dockerinit.dto.makeDockerfile.DockerfileRequest;
import com.dockerinit.dto.makeDockerfile.DockerfileResponse;
import com.dockerinit.service.DockerfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dockerfile")
@RequiredArgsConstructor
public class DockerfileController {

    private final DockerfileService service;

    @PostMapping
    public DockerfileResponse generateDockerfile(@RequestBody DockerfileRequest request) {
        String content = service.generateDockerfile(request);
        return new DockerfileResponse(content);
    }

    @GetMapping("/presets")
    public List<DockerfilePreset> getPresets() {
        return service.getAllPresets();
    }

    @GetMapping("/presets/{name}")
    public ResponseEntity<?> getPresentByName(@PathVariable String name) {
        return service.getPresentByName(name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadDockerfileZip(@RequestBody DockerfileRequest request) {
        byte[] zipBytes = service.downloadDockerfile(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename("dockerfile-template.zip").build());

        return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
    }


}
