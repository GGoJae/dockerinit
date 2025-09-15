package com.dockerinit.linux.api;

import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.linux.application.service.AutocompleteService;
import com.dockerinit.linux.dto.request.CommandAutocompleteRequest;
import com.dockerinit.linux.dto.response.autocompleteV1.LinuxAutocompleteResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/linux/autocomplete")
@RequiredArgsConstructor
public class LinuxAutocompleteController {

    private final AutocompleteService service;

    @Operation(summary = "명령어 자동완성",
            description = "입력한 문자열을 포함하는 리눅스 명령어, 옵션, 인수를 자동완성 형태로 제공합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<LinuxAutocompleteResponse>> autocomplete(@Valid CommandAutocompleteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.autocompleteCommand(request)));
    }
}
