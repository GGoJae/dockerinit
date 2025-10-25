package com.dockerinit.features.application.presetV2.shared.dto.response;

import com.dockerinit.features.application.presetV2.shared.domain.PresetKind;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

public interface PresetSuggest{
    @Value("#{target.meta.slug}")
    String getSlug();
    @Value("#{target.meta.displayName}")
    String getDisplayName();
    @Value("#{target.meta.deprecated}")
    Boolean getDeprecated();
    @Value("#{target.meta.tags}")
    Set<String> getTags();
    @Value("#{target.kind}")
    PresetKind getKind();
}
