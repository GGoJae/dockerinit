package com.dockerinit.features.support.validation.composeService;

import com.dockerinit.features.dockercompose.domain.Service;
import com.dockerinit.features.dockercompose.dto.request.ComposeRequestV1;
import com.dockerinit.features.dockercompose.dto.spec.ServiceSpecDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class ExactlyOneOfImageOrBuildValidator implements ConstraintValidator<ExactlyOneOfImageOrBuild, ServiceSpecDTO> {

    @Override
    public boolean isValid(ServiceSpecDTO service, ConstraintValidatorContext constraintValidatorContext) {
        boolean hasImage = service.image() != null && !service.image().isBlank();
        boolean hasBuild = service.build() != null;
        return hasImage ^ hasBuild;
    }
}
