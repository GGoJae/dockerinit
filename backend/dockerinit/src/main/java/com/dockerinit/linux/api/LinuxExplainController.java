package com.dockerinit.linux.api;

import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.linux.application.service.CommandExplainService;
import com.dockerinit.linux.dto.request.ExplainLineRequest;
import com.dockerinit.linux.dto.response.ExplainResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/api/linux/explain")
@RequiredArgsConstructor
public class LinuxExplainController {

    private final CommandExplainService service;

    @PostMapping()
    public ResponseEntity<? extends ApiResponse<ExplainResponse>> explain(@RequestBody @Valid ExplainLineRequest request, Locale locale) {
        Locale loc = Objects.isNull(locale) ? Locale.KOREA : locale;
        return ResponseEntity.ok(ApiResponse.success(service.explain(request.line(), loc)));
    }
}
