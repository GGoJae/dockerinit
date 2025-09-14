package com.dockerinit.features.application.dockercompose.dto.request;

import com.dockerinit.features.application.dockercompose.dto.spec.SelectionKind;
import com.dockerinit.features.application.dockercompose.dto.spec.ServiceSpecDTO;
import com.dockerinit.global.validation.Slug;
import com.dockerinit.global.validation.ValidationCollector;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

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
        String norm = (presetSlug == null) ? null : Slug.normalizeRequired(presetSlug);
        ValidationCollector.throwNowIf(kind != SelectionKind.CUSTOM && (norm == null || norm.isEmpty()),
                        "kind", "프리셋 베이스이지만 slug 를 입력하지 않았습니다", kind);

        ValidationCollector.create()
                .deferThrowIf(kind == SelectionKind.CUSTOM && norm != null)
                .topMessage("CUSTOM 선택 시 presetSlug 를 넘기면 안됩니다.")
                .withField("kind", kind)
                .withField("slug", presetSlug)
                .throwIfInvalid();

        presetSlug = norm;
    }
}
