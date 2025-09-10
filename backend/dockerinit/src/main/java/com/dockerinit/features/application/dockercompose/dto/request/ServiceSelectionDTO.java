package com.dockerinit.features.application.dockercompose.dto.request;

import com.dockerinit.features.application.dockercompose.dto.spec.SelectionKind;
import com.dockerinit.features.application.dockercompose.dto.spec.ServiceSpecDTO;
import com.dockerinit.features.support.validation.Slug;
import com.dockerinit.global.exception.InvalidInputCustomException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ServiceSelectionDTO(
        @Schema(description = "프리셋 선택 유형",
                allowableValues = {"PRESET", "PRESET_OVERRIDDEN", "CUSTOM"},
                example = "PRESET")
        @NotNull
        SelectionKind kind,

        @Schema(description = "프리셋의 slug", example = "java-spring")
        String presetSlug,

        @Valid
        @NotNull
        ServiceSpecDTO service
) {

    public ServiceSelectionDTO {
        String norm = (presetSlug == null) ? null : Slug.normalize(presetSlug);
        if (kind != SelectionKind.CUSTOM && (norm == null || norm.isEmpty())) {
            throw new InvalidInputCustomException("프리셋 베이스이지만 slug 를 입력하지 않았습니다", Map.of("kind", kind));
        }
        if (kind == SelectionKind.CUSTOM && norm != null) {
            throw new InvalidInputCustomException(
                    "CUSTOM 선택 시 presetSlug 를 넘기면 안됩니다.",
                    Map.of("kind", kind, "slug", presetSlug)
            );
        }
        presetSlug = norm;
    }
}
