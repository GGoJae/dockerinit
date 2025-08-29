package com.dockerinit.features.support.validation.composeService;

import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class ExactlyOneOfImageOrBuildValidator implements ConstraintValidator<ExactlyOneOfImageOrBuild, ComposeRequestV1.Service> {

    @Override
    public boolean isValid(ComposeRequestV1.Service service, ConstraintValidatorContext constraintValidatorContext) {
        boolean hasImage = Objects.nonNull(service.image()) && !service.image().isBlank();
        boolean hasBuild = Objects.nonNull(service.build());
        return hasImage ^ hasBuild;
    }
}
