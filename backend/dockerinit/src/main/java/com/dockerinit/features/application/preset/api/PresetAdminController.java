package com.dockerinit.features.application.preset.api;

import com.dockerinit.features.application.preset.dto.request.PresetCreateRequest;
import com.dockerinit.features.application.preset.dto.request.PresetUpdateRequest;
import com.dockerinit.features.application.preset.dto.response.PresetDetailResponse;
import com.dockerinit.features.application.preset.service.PresetAdminService;
import com.dockerinit.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/presets")
@RequiredArgsConstructor
public class PresetAdminController {

    private final PresetAdminService service;

    @PostMapping
    public ResponseEntity<ApiResponse<PresetDetailResponse>> create(@Valid @RequestBody PresetCreateRequest request) {
        // TODO 스프링 시큐리티 도입하면 Authentication auth 도 추가!

        PresetDetailResponse res = service.create(request);
        URI location = URI.create("/api/presets/" + res.slug());
        return ResponseEntity.created(location).body(ApiResponse.success(res));
    }

    @PatchMapping("/{slug}")
    public ResponseEntity<ApiResponse<PresetDetailResponse>> update(
            @PathVariable String slug,
            @Valid @RequestBody PresetUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.update(slug, request)));
    }


}
