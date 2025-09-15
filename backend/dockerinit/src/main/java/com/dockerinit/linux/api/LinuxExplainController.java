package com.dockerinit.linux.api;

import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.linux.application.service.CommandExplainService;
import com.dockerinit.linux.dto.request.ExplainLineRequest;
import com.dockerinit.linux.dto.response.explainV1.ExplainResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/linux/explain")
@RequiredArgsConstructor
public class LinuxExplainController {

    private final CommandExplainService service;

    @Operation(summary = "리눅스 명령어 설명 제공",
            description = "요청한 명령어에 대한 설명과 옵션에 대한 정보를 제공합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ExplainResponse>> explain(@RequestBody @Valid ExplainLineRequest request, Locale locale) {
        Locale loc = locale == null ? Locale.KOREA : locale;
        return ResponseEntity.ok(ApiResponse.success(service.explain(request.line(), loc)));
    }
}
