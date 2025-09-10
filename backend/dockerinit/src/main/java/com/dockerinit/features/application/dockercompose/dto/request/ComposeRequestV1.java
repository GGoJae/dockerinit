package com.dockerinit.features.application.dockercompose.dto.request;

import com.dockerinit.features.application.dockercompose.dto.spec.NetworkDTO;
import com.dockerinit.features.application.dockercompose.dto.spec.VolumeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record ComposeRequestV1(

        @Schema(description = "Compose 프로젝트 이름", example = "myapp")
        String projectName,

        @Schema(description = "서비스 목록")
        @Size(min = 1, max = 50, message = "서비스는 1~50개")
        List<@Valid ServiceSelectionDTO> services,

        @Schema(description = "네트워크 정의")
        Map<String, @Valid NetworkDTO> networks,

        @Schema(description = "볼륨 정의")
        Map<String, @Valid VolumeDTO> volumes
) {
}
