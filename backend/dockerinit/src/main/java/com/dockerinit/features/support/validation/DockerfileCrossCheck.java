package com.dockerinit.features.support.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DockerfileCrossCheckValidator.class)
public @interface DockerfileCrossCheck {
    String message() default "Dockerfile 요청 유효성 위반";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
