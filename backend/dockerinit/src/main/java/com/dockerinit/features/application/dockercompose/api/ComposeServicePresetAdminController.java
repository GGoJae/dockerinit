package com.dockerinit.features.application.dockercompose.api;

import com.dockerinit.features.application.dockercompose.dto.admin.ComposeServicePresetCreateRequest;
import com.dockerinit.features.application.dockercompose.dto.admin.ComposeServicePresetUpdateRequest;
import com.dockerinit.features.application.dockercompose.service.ComposeServicePresetAdminService;
import com.dockerinit.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// TODO 컴포즈 프리셋 만드는 어드민 컨트롤러
@RestController
@RequestMapping("/api/admin/compose/service-presets")
@RequiredArgsConstructor
public class ComposeServicePresetAdminController {

    private final ComposeServicePresetAdminService service;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody ComposeServicePresetCreateRequest request) {
        service.create(request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/{slug}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable String slug,
            @Valid @RequestBody ComposeServicePresetUpdateRequest request
    ) {
        service.update(slug, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PutMapping("/{slug}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable String slug) {
        service.setActive(slug, true);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PutMapping("/{slug}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String slug) {
        service.setActive(slug, false);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{slug}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String slug) {
        service.delete(slug);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
