package com.dockerinit.linux.api;

import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.linux.application.service.LinuxCommandService;
import com.dockerinit.linux.dto.response.LinuxCommandResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/linux")
@RequiredArgsConstructor
public class LinuxCommandController {

    private final LinuxCommandService service;

    @GetMapping("/commands")
    public ResponseEntity<ApiResponse<List<LinuxCommandResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    @GetMapping("/commands/{commandId}")
    public ResponseEntity<ApiResponse<LinuxCommandResponse>> findById(@PathVariable String commandId) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(commandId)));
    }

}
