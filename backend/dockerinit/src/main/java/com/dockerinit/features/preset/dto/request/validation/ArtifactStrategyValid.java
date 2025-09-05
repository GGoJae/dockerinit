package com.dockerinit.features.preset.dto.request.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ArtifactStrategyValidator.class)
public @interface ArtifactStrategyValid {
    String message() default "strategy에 맞는 필수 필드가 누락되었습니다.";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};

}
