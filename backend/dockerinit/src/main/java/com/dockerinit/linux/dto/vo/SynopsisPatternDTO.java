package com.dockerinit.linux.dto.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SynopsisPatternDTO(
        @NotEmpty List<@Valid LinuxCommandTokenDescriptorDTO> tokens
        ) {}
