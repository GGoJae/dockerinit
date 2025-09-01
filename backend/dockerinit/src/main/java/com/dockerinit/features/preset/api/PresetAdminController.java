package com.dockerinit.features.preset.api;

import com.dockerinit.features.preset.dto.request.PresetCreateRequest;
import com.dockerinit.features.preset.dto.response.PresetDetailResponse;
import com.dockerinit.features.preset.service.PresetAdminService;
import com.dockerinit.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/presets")
@RequiredArgsConstructor
public class PresetAdminController {

    private final PresetAdminService service;

    public ResponseEntity<ApiResponse<PresetDetailResponse>> create(@Valid @RequestBody PresetCreateRequest request) {
        // TODO 스프링 시큐리티 도입하면 Authentication auth 도 추가!!
        return null;
    }
}
