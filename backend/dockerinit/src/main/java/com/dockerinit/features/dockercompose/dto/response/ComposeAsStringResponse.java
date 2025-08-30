package com.dockerinit.features.dockercompose.dto.response;

import java.util.List;

public record ComposeAsStringResponse(
        String content,
        List<String> warnings
) {
}
