package com.dockerinit.features.support.validation.composeService;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExactlyOneOfImageOrBuildValidator.class)
public @interface ExactlyOneOfImageOrBuild {
    String message() default "image 또는 build 중 하나만 설정해야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
