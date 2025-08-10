package com.dockerinit.linux.dto.vo;

import com.dockerinit.linux.application.autoComplete.model.AcPhase;

public record ParseCtx(
        AcPhase phase,
        String baseCommand,
        String currentToken,
        String prevFlag,
        int tokenIndex
//        @Nullable ParseError error        // TODO ParseError 만들어서 에러도 전달할것인지... 그냥 Exception 던질것인지.. 선택
) {}
