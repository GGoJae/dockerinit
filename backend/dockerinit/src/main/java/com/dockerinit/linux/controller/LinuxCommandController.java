package com.dockerinit.linux.controller;

import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.linux.dto.request.LinuxAutoCompleteRequest;
import com.dockerinit.linux.dto.request.LinuxCommandGenerateRequest;
import com.dockerinit.linux.dto.request.LinuxCommandRequest;
import com.dockerinit.linux.application.service.LinuxCommandService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/linux")
@RequiredArgsConstructor
public class LinuxCommandController {

    private final LinuxCommandService service;

    @Operation(summary = "리눅스 명령어 설명 제공",
            description = "요청한 명령어에 대한 설명과 옵션에 대한 정보를 제공합니다.")
    @PostMapping("/commands/generate")
    public ResponseEntity<?> generate(@Valid @RequestBody LinuxCommandGenerateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.generate(request)));
    }


    @Operation(summary = "명령어 자동완성",
            description = "입력한 문자열을 포함하는 리눅스 명령어, 옵션, 인수를 자동완성 형태로 제공합니다.")
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@Valid LinuxAutoCompleteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.autocompleteCommand(request)));
    }

    @GetMapping("/commands")
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    @PostMapping("/commands")
    public ResponseEntity<?> createLinuxCommand(@Valid @RequestBody LinuxCommandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createCommand(request)));
    }


}
