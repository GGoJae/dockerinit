package com.dockerinit.features.support.validation.dockerfileCrossCheck;

import com.dockerinit.features.application.dockerfile.dto.request.DockerfileRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class DockerfileCrossCheckValidator implements ConstraintValidator<DockerfileCrossCheck, DockerfileRequest> {

    @Override
    public boolean isValid(DockerfileRequest r, ConstraintValidatorContext c) {
        if (r == null) return true;

        boolean hasCmd = hasValues(r.cmd());
        boolean hasEntry = hasValues(r.entrypoint());
        if (neitherExists(hasCmd, hasEntry)) {
            c.disableDefaultConstraintViolation();
            c.buildConstraintViolationWithTemplate("CMD 또는 ENTRYPOINT 중 하나는 필수입니다.")
                    .addPropertyNode("cmd")
                    .addConstraintViolation();
            return false;
        }

        if (whenWorkdirIsNotAbsolutePath(r)) {
            c.disableDefaultConstraintViolation();
            c.buildConstraintViolationWithTemplate("WORKDIR은 절대경로여야 합니다.")
                    .addPropertyNode("workdir")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private static boolean whenWorkdirIsNotAbsolutePath(DockerfileRequest r) {
        return r.workdir() != null && !r.workdir().isBlank() && !r.workdir().startsWith("/");
    }

    private static boolean neitherExists(boolean hasCmd, boolean hasEntry) {
        return !hasCmd && !hasEntry;
    }

    private boolean hasValues(List<String> list) {
        if (list == null || list.isEmpty()) return false;
        return list.stream().anyMatch(s -> s != null && !s.isBlank());
    }
}
