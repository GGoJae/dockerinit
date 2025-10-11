package com.dockerinit.features.application.presetv2.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(onConstructor_ = @PersistenceCreator)
@NoArgsConstructor
public class PresetMetrics {

    @Builder.Default
    private Long viewed = 0L;
    @Builder.Default
    private Long applied = 0L;
    @Builder.Default
    private Long copied = 0L;
    @Builder.Default
    private Long downloaded = 0L;

    public PresetMetrics inc(PresetEventType type) {
        switch (type) {
            case VIEWED -> this.viewed = safeInc(this.viewed);
            case APPLIED -> this.applied = safeInc(this.applied);
            case COPIED -> this.copied = safeInc(this.copied);
            case DOWNLOADED -> this.downloaded = safeInc(this.downloaded);
        }
        return this;
    }

    private static long safeInc(Long n) {
        return (n == null ? 0L : n) + 1L;
    }
}
