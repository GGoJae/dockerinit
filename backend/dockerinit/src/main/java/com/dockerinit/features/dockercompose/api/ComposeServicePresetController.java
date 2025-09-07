package com.dockerinit.features.dockercompose.api;

import com.dockerinit.features.dockercompose.dto.response.ComposeServicePresetDetailResponse;
import com.dockerinit.features.dockercompose.dto.response.ComposeServicePresetSummaryResponse;
import com.dockerinit.features.dockercompose.dto.spec.CategoryDTO;
import com.dockerinit.features.dockercompose.service.ComposeServicePresetService;
import com.dockerinit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "요청 컴포즈의 서비스에 대한 프리셋 목록 제공",
            description = "요청한 카테고리와 태그에 해당하는 프리셋을 리스트로 제공합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ComposeServicePresetSummaryResponse>>> list(
            @RequestParam(required = false) CategoryDTO category,
            @RequestParam(required = false) Set<String> tags,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.list(category, tags, pageable)));
    }

    @Operation(summary = "컴포즈 서비스 프리셋 세부사항 요청",
    description = "요청한 컴포즈 서비스 프리셋에 대한 디테일을 보여줍니다.")
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ComposeServicePresetDetailResponse>> get(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.get(slug)));
    }
}
