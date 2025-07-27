package com.dockerinit.controller;


import com.dockerinit.dto.apiResponse.ApiResponse;
import com.dockerinit.dto.dockerRun.DockerRunRequest;
import com.dockerinit.dto.dockerRun.DockerRunResponse;
import com.dockerinit.service.DockerCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docker")
@RequiredArgsConstructor
public class DockerController {

    private final DockerCommandService dockerCommandService;

    @PostMapping("/run")
    public ResponseEntity<?> generateRunCommand(@RequestBody DockerRunRequest request) {
        String command = dockerCommandService.generateDockerRunCommand(request);
        return ResponseEntity.ok(ApiResponse.success(command));
    }
}
