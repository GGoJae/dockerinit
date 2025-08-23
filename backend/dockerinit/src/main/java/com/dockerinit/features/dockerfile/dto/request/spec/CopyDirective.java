package com.dockerinit.features.dockerfile.dto.request.spec;

import com.dockerinit.features.support.validation.SafeRelPath;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "파일 복사 지시자 (COPY/ADD 용)")
public record CopyDirective(
        @Schema(description = "소스 경로(상대경로 권장)", example = "src/")
        @NotBlank(message = "source는 필수입니다.")
        @SafeRelPath // 상대경로만 허용, '..' / URL 금지
        String source,

        @Schema(description = "타겟 경로(절대경로)", example = "/app")
        @NotBlank(message = "target은 필수입니다.")
        @Pattern(regexp = "^/.*$", message = "target은 절대경로여야 합니다.")
        String target
) {}
