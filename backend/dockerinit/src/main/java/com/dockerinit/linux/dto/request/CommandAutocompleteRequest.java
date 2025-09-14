package com.dockerinit.linux.dto.request;

import com.dockerinit.global.validation.ValidationCollector;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import static com.dockerinit.global.constants.ErrorMessage.LINUX_CURSOR_OUT_OF_RANGE;

public record CommandAutocompleteRequest(
        @Schema(description = "사용자가 입력한 전체 라인", example = "ping -c ")
        @NotBlank(message = "line 은 필수입니다.")
        String line,

        @Schema(
                description = "커서 위치(생략 시 line 길이와 동일)",
                example = "6"
        )
        @Min(value = 0, message = "cursor 는 0 이상이어야 합니다.")
        Integer cursor
) {

    public CommandAutocompleteRequest {

        cursor = (cursor == null) ? line.length() : cursor;

        ValidationCollector.create()
                .rejectIf(cursor < 0 || line.length() < cursor,
                        "cursor",
                        LINUX_CURSOR_OUT_OF_RANGE,
                        cursor
                )
                .withField("lineLength", line)
                .throwIfInvalid();
    }


}
