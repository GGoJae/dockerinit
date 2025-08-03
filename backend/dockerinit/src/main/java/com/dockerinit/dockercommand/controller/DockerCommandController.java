package com.dockerinit.dockercommand.controller;


import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.dockercommand.dto.DockerRunRequest;
import com.dockerinit.dockercommand.service.DockerCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docker")
@RequiredArgsConstructor
public class DockerCommandController {

    private final DockerCommandService dockerCommandService;

    @PostMapping("/run")
    public ResponseEntity<?> generateRunCommand(@RequestBody DockerRunRequest request) {
        String command = dockerCommandService.generateDockerRunCommand(request);
        return ResponseEntity.ok(ApiResponse.success(command));
    }
}
