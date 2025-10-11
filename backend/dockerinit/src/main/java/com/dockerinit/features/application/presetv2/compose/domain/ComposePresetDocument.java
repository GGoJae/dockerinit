package com.dockerinit.features.application.presetv2.compose.domain;

import com.dockerinit.features.application.dockercompose.domain.composeCustom.ComposePlan;
import com.dockerinit.features.application.presetv2.shared.domain.PresetKind;
import com.dockerinit.features.application.presetv2.shared.domain.PresetMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("compose_presets")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ComposePresetDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private PresetMeta meta;

    private ComposePlan plan;

    private PresetKind kind = PresetKind.COMPOSE;
}
