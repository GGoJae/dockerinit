package com.dockerinit.linux.dto.response.explainV1;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "노트 레벨")
public enum NoteLevel {
    INFO, WARNING, DANGER
}
