package com.dockerinit.linux.api;

import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.linux.application.service.LinuxCommandService;
import com.dockerinit.linux.dto.request.CreateLinuxCommandRequest;
import com.dockerinit.linux.dto.response.LinuxCommandResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class LinuxCommandAdminController {

    private final LinuxCommandService service;
    // TODO 관리자용 컨트롤러 crud 여기로

    @PostMapping("/commands")
    public ResponseEntity<ApiResponse<LinuxCommandResponse>> createLinuxCommand(@Valid @RequestBody CreateLinuxCommandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createCommand(request)));
    }

}
