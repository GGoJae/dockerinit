package com.dockerinit.linux.dto.request.spec;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SynopsisPatternSpecDTO(
        @NotEmpty List<@Valid TokenDescriptorSpecDTO> tokens
        ) {}
