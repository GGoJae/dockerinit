package com.dockerinit.linux.dto.response;

import com.dockerinit.linux.application.autoComplete.model.AcPhase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "자동완성 응답")
public record LinuxAutoCompleteResponse(
        AcPhase phase,
        String  base,         // 확정된 명령어
        String  current,      // 커서가 위치한 토큰
        List<Suggestion> suggestions
) {}
