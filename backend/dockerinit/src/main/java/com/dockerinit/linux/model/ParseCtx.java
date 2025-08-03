package com.dockerinit.linux.model;

public record ParseCtx(AcPhase phase, String baseCommand,
                               String currentToken, String prevFlag) {}
