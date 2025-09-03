package com.dockerinit.features.dockercompose.api;

import com.dockerinit.features.dockercompose.dto.response.ComposeServicePresetDetailResponse;
import com.dockerinit.features.dockercompose.dto.response.ComposeServicePresetSummaryResponse;
import com.dockerinit.features.dockercompose.dto.spec.CategoryDTO;
import com.dockerinit.features.dockercompose.service.ComposeServicePresetService;
import com.dockerinit.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/compose/service-presets")
@RequiredArgsConstructor
public class ComposeServicePresetController {

    private final ComposeServicePresetService service;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ComposeServicePresetSummaryResponse>>> list(
            @RequestParam(required = false) CategoryDTO category,
            @RequestParam(required = false) Set<String> tags,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.list(category, tags, pageable)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ComposeServicePresetDetailResponse>> get(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.get(slug)));
    }
}
