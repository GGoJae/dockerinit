package com.dockerinit.controller;

import com.dockerinit.dto.linuxCommand.LinuxCommandRequest;
import com.dockerinit.dto.linuxCommand.LinuxCommandResponse;
import com.dockerinit.service.LinuxCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/linux")
@RequiredArgsConstructor
public class LinuxCommandController {

    private final LinuxCommandService service;

    @PostMapping("/commands")
    public LinuxCommandResponse generate(@RequestBody LinuxCommandRequest request) {
        return service.generate(request);
    }

    @GetMapping("/autocomplete")
    public List<String> autocomplete(@RequestParam String prefix) {
        return service.autocompleteCommand(prefix);
    }

    @GetMapping("/autocomplete-options")
    public List<String> autocompleteOptions(@RequestParam String command,
                                            @RequestParam(required = false, defaultValue = "") String prefix) {
        return service.autocompleteOptions(command, prefix);
    }


}
