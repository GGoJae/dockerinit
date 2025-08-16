package com.dockerinit.linux.dto.response.explainV1;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오퍼랜드 타입")
public enum OperandType {
    HOST, FILE, DIRECTORY, PATH, PATTERN, RAW
}
