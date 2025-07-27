package com.dockerinit.controller;

import com.dockerinit.dto.apiResponse.ApiResponse;
import com.dockerinit.dto.linuxCommand.LinuxCommandRequest;
import com.dockerinit.dto.linuxCommand.LinuxCommandResponse;
import com.dockerinit.service.LinuxCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/linux")
@RequiredArgsConstructor
public class LinuxCommandController {

    private final LinuxCommandService service;

    @PostMapping("/commands")
    public ResponseEntity<?> generate(@RequestBody LinuxCommandRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.generate(request)));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String prefix) {
        return ResponseEntity.ok(ApiResponse.success(service.autocompleteCommand(prefix)));
    }

    @GetMapping("/autocomplete-options")
    public ResponseEntity<?> autocompleteOptions(@RequestParam String command,
                                            @RequestParam(required = false, defaultValue = "") String prefix) {
        return ResponseEntity.ok(ApiResponse.success(service.autocompleteOptions(command, prefix)));
    }


}
