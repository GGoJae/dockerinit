package com.dockerinit.linux.api.admin;

import com.dockerinit.global.response.ApiResponse;
import com.dockerinit.linux.application.service.LinuxCommandService;
import com.dockerinit.linux.crawling.LinuxManImportService;
import com.dockerinit.linux.dto.request.CreateLinuxCommandRequest;
import com.dockerinit.linux.dto.response.batchResult.BatchResult;
import com.dockerinit.linux.dto.response.doc.LinuxCommandDocResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/linux")
@RequiredArgsConstructor
public class LinuxCommandAdminController {

    private final LinuxCommandService service;
    private final LinuxManImportService importer;
    // TODO 관리자용 컨트롤러 crud 여기로

    @PostMapping("/commands")
    public ResponseEntity<ApiResponse<LinuxCommandDocResponse>> createOne(@Valid @RequestBody CreateLinuxCommandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createCommand(request)));
    }

    @PostMapping("/commands/import")
    public ResponseEntity<ApiResponse<LinuxCommandDocResponse>> importOne(@RequestParam String command) {
        return ResponseEntity.ok(ApiResponse.success(importer.importKo(command)));
    }

    @PostMapping("/commands/imposrt/batch")
    public ResponseEntity<ApiResponse<BatchResult>> importBatch(@RequestBody List<String> commands) {
        return ResponseEntity.ok(ApiResponse.success(importer.importKoBatch(commands)));
    }

}
