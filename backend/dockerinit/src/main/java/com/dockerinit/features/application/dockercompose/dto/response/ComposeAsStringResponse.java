package com.dockerinit.features.application.dockercompose.dto.response;

import java.util.List;

public record ComposeAsStringResponse(
        String content,
        List<String> warnings
) {
}
