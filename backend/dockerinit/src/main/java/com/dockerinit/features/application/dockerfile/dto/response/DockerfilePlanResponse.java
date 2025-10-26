package com.dockerinit.features.application.dockerfile.dto.response;

import com.dockerinit.features.application.dockerfile.domain.vo.CopyEntry;
import com.dockerinit.features.application.dockerfile.domain.vo.Healthcheck;
import com.dockerinit.features.model.EnvMode;
import com.dockerinit.features.model.FileType;

import java.util.List;
import java.util.Map;
import java.util.Set;
// TODO 필드 response 용 dto 만들어서 반납하기
public record DockerfilePlanResponse(
        String baseImage,
        String workdir,
        List<CopyEntry> copy,
        List<CopyEntry> add,
        EnvMode envMode,
        Map<String, String> envVars,
        List<Integer> expose,
        List<String> cmd,
        List<String> run,
        List<String> entrypoint,
        Map<String, String> label,
        String user,
        Map<String, String> args,
        Healthcheck healthcheck,
        List<String> volume,
        List<String> warnings,
        Set<FileType> targets
) {
}
