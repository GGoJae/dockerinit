package com.dockerinit.features.application.presetV2.shared.dto.request;

import com.dockerinit.features.application.presetV2.shared.domain.PresetEventType;

public record PresetEventRequest(PresetEventType event) {
}
