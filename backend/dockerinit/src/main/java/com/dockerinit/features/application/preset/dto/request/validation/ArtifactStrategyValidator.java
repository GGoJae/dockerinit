package com.dockerinit.features.application.preset.dto.request.validation;

import com.dockerinit.features.application.preset.dto.request.PresetArtifactRequest;
import com.dockerinit.features.application.preset.dto.spec.ContentStrategyDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ArtifactStrategyValidator implements ConstraintValidator<ArtifactStrategyValid, PresetArtifactRequest> {
    @Override
    public boolean isValid(PresetArtifactRequest ar, ConstraintValidatorContext ctx) {
        if (ar == null) return true;
        if (ar.strategy() == ContentStrategyDTO.EMBEDDED) {
            return ar.inlineContent() != null && !ar.inlineContent().isBlank();
        }
        if (ar.strategy() == ContentStrategyDTO.OBJECT_STORAGE) {
            return ar.storageProvider() != null && !ar.storageProvider().isBlank()
                    && ar.storageKey() != null && !ar.storageKey().isBlank();
        }
        return false;
    }
}
