package com.dockerinit.global.validation.safeRefPath;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SafeRelPathValidator implements ConstraintValidator<SafeRelPath, String> {


    @Override
    public boolean isValid(String v, ConstraintValidatorContext c) {
        if (v == null || v.isBlank()) return true;

        if (v.startsWith("/")) return false;

        if (v.contains("..")) return false;         // 상위 디렉터리로 탈출 금지

        if (v.startsWith("~")) return false;

        if (v.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) return false;

        return true;
    }
}
