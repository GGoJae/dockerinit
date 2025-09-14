package com.dockerinit.global.validation.safeRefPath;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeRelPathValidator.class)
public @interface SafeRelPath {
    String message() default "상대경로만 허용하며 '..' 또는 URL/홈디렉터리(~) 경로는 금지됩니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
