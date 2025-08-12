package com.dockerinit.linux.dto.request.spec;

import com.dockerinit.linux.domain.syntax.TokenType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TokenDescriptorSpecDTO(
        @NotNull
        TokenType tokenType,
        boolean repeat,
        boolean optional,
        @Size(max = 200) String description
) {}
