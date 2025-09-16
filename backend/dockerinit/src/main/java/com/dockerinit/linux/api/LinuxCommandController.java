package com.dockerinit.linux.api;

import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.linux.application.service.LinuxCommandService;
import com.dockerinit.linux.dto.response.doc.LinuxCommandDocResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/linux")
@RequiredArgsConstructor
public class LinuxCommandController {

    private final LinuxCommandService service;

    @GetMapping("/commands")
    public ResponseEntity<ApiResponse<Page<LinuxCommandDocResponse>>> findAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.getAll(pageable)));
    }

    @GetMapping("/commands/{commandId}")
    public ResponseEntity<ApiResponse<LinuxCommandDocResponse>> findById(@PathVariable String commandId) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(commandId)));
    }

}
