package com.dockerinit.linux.dto.response.batchResult;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "배치 처리 결과")
public record BatchResult(
        @Schema(description = "항목 목록 (입력 순서 유지)")
        List<Item> items,
        @Schema(description = "총 개수")
        int total,
        @Schema(description = "성공 개수")
        int succeeded,
        @Schema(description = "실패 개수")
        int failed
) {
    public static Collector collector() {
        return new Collector();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "개별 항목 결과")
    public record Item(
            @Schema(description = "입력 순서 (0-base)")
            int order,
            @Schema(description = "명령어")
            String command,
            @Schema(description = "성공 여부")
            boolean success,
            @Schema(description = "오류 타입 (실패 시)")
            String errorType,
            @Schema(description = "메시지 (실패 시)")
            String message
    ) {}


    public static class Collector {
        private final List<Item> items = new ArrayList<>();
        private int ok = 0, fail = 0;

        public Collector success(int order, String command) {
            items.add(new Item(order, command, true, null, null));
            ok++;
            return this;
        }

        public Collector fail(int order, String command, Throwable e) {
            items.add(new Item(order, command, false,
                    e == null ? null : e.getClass().getSimpleName(),
                    safeMsg(e)));
            fail++;
            return this;
        }

        public BatchResult completed() {
            return new BatchResult(List.copyOf(items), ok + fail, ok, fail);
        }

        private static String safeMsg(Throwable e) {
            if (e == null) return null;
            String m = String.valueOf(e.getMessage());
            return m.length() > 300 ? m.substring(0, 297) + "..." : m;
        }
    }

}
