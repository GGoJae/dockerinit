package com.dockerinit.linux.dto.vo;

import com.dockerinit.linux.domain.syntax.TokenType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LinuxCommandTokenDescriptorDTO(
        @NotNull
        TokenType tokenType,
        boolean repeat,
        boolean optional,
        @Size(max = 200) String description
) {}
