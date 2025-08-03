package com.dockerinit.service.linux;

import com.dockerinit.vo.Linux.AcPhase;

public record ParseCtx(AcPhase phase, String baseCommand,
                               String currentToken, String prevFlag) {}
